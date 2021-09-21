package ru.wb.perevozka.ui.courierunloading

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.CourierLoadingBoxesItemLayoutBinding

class CourierUnloadingBoxesAdapter(
    context: Context,
    private val items: MutableList<CourierUnloadingBoxesItem>,
    private val onItemClickCallBack: OnItemClickCallBack,
) : RecyclerView.Adapter<CourierUnloadingBoxesAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    interface OnItemClickCallBack {
        fun onItemClick(index: Int, isChecked: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.courier_loading_boxes_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (index, qrCode, info, isChecked) = items[position]
        holder.binding.index.text = index
        holder.binding.qrCode.text = qrCode
        holder.binding.info.text = info
        holder.binding.checked.isChecked = isChecked
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItem(index: Int, item: CourierUnloadingBoxesItem) {
        if (items.size > index) items[index] = item
    }

    inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView),
        View.OnClickListener {
        var binding: CourierLoadingBoxesItemLayoutBinding =
            CourierLoadingBoxesItemLayoutBinding.bind(rootView)

        override fun onClick(v: View) {
            binding.checked.isChecked = !binding.checked.isChecked
            onItemClickCallBack.onItemClick(adapterPosition, binding.checked.isChecked)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

}