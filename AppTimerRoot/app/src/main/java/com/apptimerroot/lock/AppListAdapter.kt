package com.apptimerroot.lock

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlin.math.max
import kotlin.math.min

/**
 * Installed-app picker. Renders either as a checkbox list (default) or as a
 * compact tile grid. Both modes share the same selection state and the same
 * [toggle] logic, so tapping a tile does exactly what tapping a list row does.
 */
class AppListAdapter(
    private val items: MutableList<AppInfo>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.app_icon)
        val label: TextView = v.findViewById(R.id.app_label)
        val pkg: TextView = v.findViewById(R.id.app_pkg)
        val check: CheckBox = v.findViewById(R.id.app_check)
    }

    class TileVH(v: View) : RecyclerView.ViewHolder(v) {
        val body: View = v.findViewById(R.id.tile_body)
        val icon: ImageView = v.findViewById(R.id.tile_icon)
        val label: TextView = v.findViewById(R.id.tile_label)
        val check: ImageView = v.findViewById(R.id.tile_check)
    }

    /** When true, items render as compact grid tiles. */
    var gridMode: Boolean = false
        private set

    // Tile geometry in px, computed by the activity from the RecyclerView size.
    private var tileWidthPx = 0
    private var tileHeightPx = 0

    fun setGridMode(enabled: Boolean) {
        if (gridMode == enabled) return
        gridMode = enabled
        notifyDataSetChanged()
    }

    /** Update tile size; returns true if it changed (and rebinds). */
    fun setTileSize(widthPx: Int, heightPx: Int): Boolean {
        if (widthPx <= 0 || heightPx <= 0) return false
        if (widthPx == tileWidthPx && heightPx == tileHeightPx) return false
        tileWidthPx = widthPx
        tileHeightPx = heightPx
        if (gridMode) notifyDataSetChanged()
        return true
    }

    override fun getItemViewType(position: Int): Int =
        if (gridMode) VIEW_TILE else VIEW_ROW

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TILE) {
            TileVH(inflater.inflate(R.layout.item_app_tile, parent, false))
        } else {
            VH(inflater.inflate(R.layout.item_app, parent, false))
        }
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TileVH -> bindTile(holder, items[position])
            is VH -> bindRow(holder, items[position])
        }
    }

    private fun bindRow(holder: VH, a: AppInfo) {
        holder.label.text = a.label
        holder.pkg.text = a.packageName
        holder.icon.setImageDrawable(a.icon)
        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked = a.checked
        holder.check.setOnCheckedChangeListener { _, isChecked ->
            a.checked = isChecked
        }
        holder.itemView.setOnClickListener {
            holder.check.isChecked = !holder.check.isChecked
        }
    }

    private fun bindTile(holder: TileVH, a: AppInfo) {
        applyTileGeometry(holder)
        holder.icon.setImageDrawable(a.icon)
        holder.label.text = a.label
        renderTileState(holder, a)
        holder.itemView.setOnClickListener {
            val pos = holder.bindingAdapterPosition
            if (pos == RecyclerView.NO_POSITION) return@setOnClickListener
            toggle(items[pos])
            renderTileState(holder, items[pos])
        }
    }

    /** Same effect as tapping a list row: flip the app's selection. */
    private fun toggle(a: AppInfo) {
        a.checked = !a.checked
    }

    private fun renderTileState(holder: TileVH, a: AppInfo) {
        holder.check.visibility = if (a.checked) View.VISIBLE else View.GONE
        holder.body.setBackgroundResource(if (a.checked) R.drawable.bg_tile_selected else 0)
        holder.itemView.contentDescription = if (a.checked) {
            holder.itemView.context.getString(R.string.tile_selected_desc, a.label)
        } else {
            a.label
        }
        holder.itemView.isSelected = a.checked
    }

    /**
     * Size the tile to exactly 1/6 of the list width and 1/8 of its height,
     * then fit icon + a single small label line inside without clipping.
     */
    private fun applyTileGeometry(holder: TileVH) {
        val v = holder.itemView
        val res = v.resources
        val dm = res.displayMetrics
        val w = if (tileWidthPx > 0) tileWidthPx else (56 * dm.density).toInt()
        val h = if (tileHeightPx > 0) tileHeightPx else (64 * dm.density).toInt()

        val lp = v.layoutParams ?: RecyclerView.LayoutParams(w, h)
        lp.width = w
        lp.height = h
        v.layoutParams = lp

        // Inner space available after outer (2dp) + body (2dp) padding.
        val pad = ((2 + 2) * 2 * dm.density).toInt()
        val innerW = max(1, w - pad)
        val innerH = max(1, h - pad)

        // Label: ~22% of inner height, clamped to 7..11sp.
        val minTextPx = 7f * dm.scaledDensity
        val maxTextPx = 11f * dm.scaledDensity
        val textPx = (innerH * 0.22f).coerceIn(minTextPx, maxTextPx)
        holder.label.setTextSize(TypedValue.COMPLEX_UNIT_PX, textPx)
        val labelLineH = holder.label.paint.fontMetricsInt.let { it.descent - it.ascent }
        val labelMargin = (1 * dm.density).toInt()

        // Icon: whatever is left vertically, bounded by width.
        val iconSize = max(1, min(innerW, innerH - labelLineH - labelMargin))
        val ilp = holder.icon.layoutParams
        if (ilp.width != iconSize || ilp.height != iconSize) {
            ilp.width = iconSize
            ilp.height = iconSize
            holder.icon.layoutParams = ilp
        }

        // Checkmark badge scales with the tile but stays small.
        val badge = (min(w, h) * 0.28f).toInt().coerceIn(
            (10 * dm.density).toInt(), (18 * dm.density).toInt()
        )
        val clp = holder.check.layoutParams
        if (clp.width != badge || clp.height != badge) {
            clp.width = badge
            clp.height = badge
            holder.check.layoutParams = clp
        }
    }

    fun selectedPackages(): List<String> =
        items.filter { it.checked }.map { it.packageName }

    fun setItems(newItems: List<AppInfo>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    /** Check every app whose package is in [packages]. */
    fun checkPackages(packages: Set<String>) {
        var changed = false
        for (a in items) {
            if (a.packageName in packages && !a.checked) {
                a.checked = true
                changed = true
            }
        }
        if (changed) notifyDataSetChanged()
    }

    /**
     * Uncheck apps in [packages] unless the package is also in [keepChecked]
     * (claimed by another still-active category).
     */
    fun uncheckPackagesExcept(packages: Set<String>, keepChecked: Set<String>) {
        var changed = false
        for (a in items) {
            if (a.packageName in packages &&
                a.packageName !in keepChecked &&
                a.checked
            ) {
                a.checked = false
                changed = true
            }
        }
        if (changed) notifyDataSetChanged()
    }

    fun allApps(): List<AppInfo> = items.toList()

    companion object {
        private const val VIEW_ROW = 0
        private const val VIEW_TILE = 1
        const val GRID_COLUMNS = 6
        const val GRID_ROWS = 8
    }
}
