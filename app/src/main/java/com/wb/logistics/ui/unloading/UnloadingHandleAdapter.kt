package com.wb.logistics.ui.unloading

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.wb.logistics.R
import com.wb.logistics.databinding.UnloadingHandleItemLayoutBinding

class UnloadingHandleAdapter(context: Context, private val items: List<String>) :
    ArrayAdapter<String>(context, R.layout.unloading_handle_item_layout) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            convertView =
                LayoutInflater.from(context).inflate(R.layout.unloading_handle_item_layout, null)
            holder = ViewHolder(convertView)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        val item = items[position]
        holder.binding.barcode.text = item
        return convertView!!
    }

    override fun getCount(): Int {
        return items.size
    }

    private inner class ViewHolder(rootView: View?) {
        var binding = UnloadingHandleItemLayoutBinding.bind(rootView!!)
    }
}