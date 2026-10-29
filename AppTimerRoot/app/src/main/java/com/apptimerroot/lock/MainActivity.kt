package com.apptimerroot.lock

import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: AppListAdapter
    private lateinit var statusView: TextView
    private lateinit var durationInput: android.widget.EditText
    private lateinit var startButton: Button
    private lateinit var categoryChips: ChipGroup
    private lateinit var appList: RecyclerView
    private lateinit var viewModeToggle: ImageButton

    /** Currently toggled-on categories (multi-select). */
    private val activeCategories = mutableSetOf<AppCategory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusView = findViewById(R.id.status_text)
        durationInput = findViewById(R.id.duration_minutes)
        startButton = findViewById(R.id.start_button)
        categoryChips = findViewById(R.id.category_chips)

        appList = findViewById(R.id.app_list)
        viewModeToggle = findViewById(R.id.view_mode_toggle)
        adapter = AppListAdapter(mutableListOf())
        appList.adapter = adapter

        // Recompute grid tile size whenever the list area is (re)measured.
        appList.addOnLayoutChangeListener { _, l, t, r, b, ol, ot, or_, ob ->
            if (r - l != or_ - ol || b - t != ob - ot) appList.post { updateTileSize() }
        }

        applyViewMode(prefs().getBoolean(PREF_GRID_MODE, false))
        viewModeToggle.setOnClickListener {
            val grid = !adapter.gridMode
            prefs().edit().putBoolean(PREF_GRID_MODE, grid).apply()
            applyViewMode(grid)
        }

        findViewById<Button>(R.id.credit_button).setOnClickListener { openCreditLink() }

        buildCategoryChips()
        startButton.setOnClickListener { onStartLock() }

        refreshStatus()
        loadApps()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
    }

    private fun prefs() = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

    /** Switch between the default list and the 6-column tile grid. */
    private fun applyViewMode(grid: Boolean) {
        appList.layoutManager = if (grid) {
            GridLayoutManager(this, AppListAdapter.GRID_COLUMNS)
        } else {
            LinearLayoutManager(this)
        }
        updateTileSize()
        adapter.setGridMode(grid)
        viewModeToggle.setImageResource(if (grid) R.drawable.ic_view_list else R.drawable.ic_view_grid)
        viewModeToggle.contentDescription =
            getString(if (grid) R.string.view_as_list else R.string.view_as_grid)
    }

    /** Tile = 1/6 of list width x 1/8 of list height, so 48 tiles fit per screen. */
    private fun updateTileSize() {
        val w = appList.width - appList.paddingLeft - appList.paddingRight
        val h = appList.height - appList.paddingTop - appList.paddingBottom
        if (w <= 0 || h <= 0) return
        adapter.setTileSize(w / AppListAdapter.GRID_COLUMNS, h / AppListAdapter.GRID_ROWS)
    }

    private fun openCreditLink() {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.credit_url)))
            .addCategory(Intent.CATEGORY_BROWSABLE)
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, R.string.err_no_browser, Toast.LENGTH_SHORT).show()
        } catch (e: SecurityException) {
            Toast.makeText(this, R.string.err_no_browser, Toast.LENGTH_SHORT).show()
        }
    }

    private fun buildCategoryChips() {
        categoryChips.removeAllViews()
        for (cat in AppCategory.entries) {
            val chip = Chip(this).apply {
                text = cat.label
                isCheckable = true
                isChecked = false
                tag = cat
                setOnCheckedChangeListener { _, isChecked ->
                    onCategoryToggled(cat, isChecked)
                }
            }
            categoryChips.addView(chip)
        }
    }

    private fun onCategoryToggled(category: AppCategory, enabled: Boolean) {
        val apps = adapter.allApps()
        if (apps.isEmpty()) return

        val matched = AppCategories.matchingPackages(category, apps)

        if (enabled) {
            activeCategories.add(category)
            adapter.checkPackages(matched)
        } else {
            activeCategories.remove(category)
            // Keep apps still claimed by any remaining active category.
            val keep = activeCategories
                .flatMap { AppCategories.matchingPackages(it, apps) }
                .toSet()
            adapter.uncheckPackagesExcept(matched, keep)
        }
    }

    private fun loadApps() {
        thread {
            val pm = packageManager
            val launchable = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                .asSequence()
                .filter { it.packageName != packageName }           // never lock ourselves
                .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
                .map {
                    AppInfo(
                        packageName = it.packageName,
                        label = pm.getApplicationLabel(it).toString(),
                        icon = try { pm.getApplicationIcon(it) } catch (e: Exception) { null }
                    )
                }
                .sortedBy { it.label.lowercase() }
                .toList()
            runOnUiThread {
                adapter.setItems(launchable)
                // Re-apply any already-active category selections after reload.
                if (activeCategories.isNotEmpty()) {
                    val keep = activeCategories
                        .flatMap { AppCategories.matchingPackages(it, launchable) }
                        .toSet()
                    adapter.checkPackages(keep)
                }
            }
        }
    }

    private fun onStartLock() {
        val minutes = durationInput.text.toString().trim().toLongOrNull()
        if (minutes == null || minutes <= 0) {
            Toast.makeText(this, R.string.err_duration, Toast.LENGTH_SHORT).show()
            return
        }
        val pkgs = adapter.selectedPackages()
        if (pkgs.isEmpty()) {
            Toast.makeText(this, R.string.err_no_apps, Toast.LENGTH_SHORT).show()
            return
        }

        startButton.isEnabled = false
        statusView.setText(R.string.status_working)

        thread {
            if (!RootUtils.isRootAvailable()) {
                runOnUiThread {
                    startButton.isEnabled = true
                    statusView.setText(R.string.err_no_root)
                    Toast.makeText(this, R.string.err_no_root, Toast.LENGTH_LONG).show()
                }
                return@thread
            }

            val now = System.currentTimeMillis()
            val unlockAt = now + TimeUnit.MINUTES.toMillis(minutes)
            val state = LockState(pkgs, unlockAt, now)

            // 1. Persist shared state for the Magisk boot daemon.
            RootUtils.writeState(state.toJson())
            // 2. Apply the freeze immediately.
            RootUtils.freezeAll(pkgs)
            // 3. Start the in-app foreground watchdog service.
            val svc = Intent(this, LockService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(svc)
            } else {
                startService(svc)
            }

            runOnUiThread {
                startButton.isEnabled = true
                refreshStatus()
                Toast.makeText(this, R.string.lock_started, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun refreshStatus() {
        thread {
            val state = LockState.fromJson(RootUtils.readState())
            val text = when {
                state == null -> getString(R.string.status_idle)
                state.active -> {
                    val mins = TimeUnit.MILLISECONDS.toMinutes(state.remainingMs())
                    getString(
                        R.string.status_locked,
                        state.lockedPackages.size,
                        mins
                    )
                }
                else -> {
                    // Timer elapsed (or escape hatch used). Release everything.
                    RootUtils.unfreezeAll(state.lockedPackages)
                    RootUtils.clearState()
                    getString(R.string.status_released)
                }
            }
            runOnUiThread { statusView.text = text }
        }
    }

    companion object {
        private const val PREFS_NAME = "ui_prefs"
        private const val PREF_GRID_MODE = "grid_mode"
    }
}
