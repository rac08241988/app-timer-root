package com.apptimerroot.lock

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

    /** Currently toggled-on categories (multi-select). */
    private val activeCategories = mutableSetOf<AppCategory>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusView = findViewById(R.id.status_text)
        durationInput = findViewById(R.id.duration_minutes)
        startButton = findViewById(R.id.start_button)
        categoryChips = findViewById(R.id.category_chips)

        val rv: RecyclerView = findViewById(R.id.app_list)
        rv.layoutManager = LinearLayoutManager(this)
        adapter = AppListAdapter(mutableListOf())
        rv.adapter = adapter

        buildCategoryChips()
        startButton.setOnClickListener { onStartLock() }

        refreshStatus()
        loadApps()
    }

    override fun onResume() {
        super.onResume()
        refreshStatus()
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
}
