package com.apptimerroot.lock

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

/** Simple checkbox list of installed apps. */
class AppListAdapter(
    private val items: MutableList<AppInfo>
) : RecyclerView.Adapter<AppListAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val icon: ImageView = v.findViewById(R.id.app_icon)
        val label: TextView = v.findViewById(R.id.app_label)
        val pkg: TextView = v.findViewById(R.id.app_pkg)
        val check: CheckBox = v.findViewById(R.id.app_check)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_app, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val a = items[position]
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

    fun selectedPackages(): List<String> =
        items.filter { it.checked }.map { it.packageName }

    fun setItems(newItems: List<AppInfo>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
