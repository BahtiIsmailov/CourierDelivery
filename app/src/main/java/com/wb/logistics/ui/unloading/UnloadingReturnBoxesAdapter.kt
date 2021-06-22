package com.wb.logistics.ui.unloading

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.wb.logistics.R
import com.wb.logistics.databinding.UnloadingBoxesItemReturnLayoutBinding

class UnloadingReturnBoxesAdapter(
    context: Context,
    private val items: MutableList<UnloadingReturnBoxesItem>,
    private val onItemClickCallBack: OnItemClickCallBack,
) : RecyclerView.Adapter<UnloadingReturnBoxesAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    interface OnItemClickCallBack {
        fun onItemClick(index: Int, isChecked: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.unloading_boxes_item_return_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UnloadingReturnBoxesAdapter.ViewHolder, position: Int) {
        val (number, barcode, data, isChecked) = items[position]
        holder.binding.number.text = number
        holder.binding.box.text = barcode
        holder.binding.data.text = data
        holder.binding.checked.isChecked = isChecked
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItem(index: Int, item: UnloadingReturnBoxesItem) {
        if (items.size > index) items[index] = item
    }

    inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView),
        View.OnClickListener {
        var binding = UnloadingBoxesItemReturnLayoutBinding.bind(rootView)

        override fun onClick(v: View) {
            binding.checked.isChecked = !binding.checked.isChecked
            onItemClickCallBack.onItemClick(adapterPosition, binding.checked.isChecked)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

}