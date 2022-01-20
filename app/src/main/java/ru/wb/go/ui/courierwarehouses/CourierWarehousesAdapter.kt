package ru.wb.go.ui.courierwarehouses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.databinding.CourierWarehouseItemLayoutBinding

class CourierWarehousesAdapter(
    context: Context,
    private val items: MutableList<CourierWarehouseItem>,
    private val onItemClickCallBack: OnItemClickCallBack,
) : RecyclerView.Adapter<CourierWarehousesAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    interface OnItemClickCallBack {
        fun onItemClick(index: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.courier_warehouse_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (id, name, address, isSelected) = items[position]
        holder.binding.nameWarehouse.text = name
        holder.binding.fullAddressWarehouse.text = address
        holder.binding.selectedBackground.visibility =
            if (isSelected) View.VISIBLE else View.INVISIBLE
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItem(index: Int, item: CourierWarehouseItem) {
        if (items.size > index) items[index] = item
    }

    inner class ViewHolder(rootView: View) :
        RecyclerView.ViewHolder(rootView) {

        var binding = CourierWarehouseItemLayoutBinding.bind(rootView)

        init {
            binding.warehouseLayout.setOnClickListener {
                onItemClickCallBack.onItemClick(adapterPosition)
            }
        }
    }

    fun clear() {
        items.clear()
    }

    fun addItems(items: List<CourierWarehouseItem>) {
        this.items.addAll(items)
    }

}