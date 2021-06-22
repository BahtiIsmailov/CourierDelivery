package com.wb.logistics.ui.unloading

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wb.logistics.R
import com.wb.logistics.databinding.UnloadingBoxesItemLayoutBinding

class UnloadingBoxesAdapter(context: Context, private val items: MutableList<String>) :
    RecyclerView.Adapter<UnloadingBoxesAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.unloading_boxes_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UnloadingBoxesAdapter.ViewHolder, position: Int) {
        holder.binding.barcode.text = items[position]
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        var binding = UnloadingBoxesItemLayoutBinding.bind(rootView)
    }

}