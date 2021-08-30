package ru.wb.perevozka.ui.courierloading

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.DcLoadingBoxesItemLayoutBinding

class CourierScannerLoadingBoxesAdapter(
    context: Context,
    private val items: MutableList<CourierScannerLoadingBoxesItem>,
    private val onItemClickCallBack: OnItemClickCallBack,
) : RecyclerView.Adapter<CourierScannerLoadingBoxesAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    interface OnItemClickCallBack {
        fun onItemClick(index: Int, isChecked: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.dc_loading_boxes_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (barcode, unnamedBarcode, address, isChecked) = items[position]
        holder.binding.box.text = unnamedBarcode
        holder.binding.address.text = address
        holder.binding.checked.isChecked = isChecked
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItem(index: Int, item: CourierScannerLoadingBoxesItem) {
        if (items.size > index) items[index] = item
    }

    inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView),
        View.OnClickListener {
        var binding: DcLoadingBoxesItemLayoutBinding =
            DcLoadingBoxesItemLayoutBinding.bind(rootView)

        override fun onClick(v: View) {
            binding.checked.isChecked = !binding.checked.isChecked
            onItemClickCallBack.onItemClick(adapterPosition, binding.checked.isChecked)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

}