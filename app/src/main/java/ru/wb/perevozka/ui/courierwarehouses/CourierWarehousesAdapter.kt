package ru.wb.perevozka.ui.courierwarehouses

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.CourierWarehouseItemLayoutBinding

class CourierWarehousesAdapter(
    context: Context,
    private val items: MutableList<CourierWarehousesItem>,
    private val onItemClickCallBack: OnItemClickCallBack,
) : RecyclerView.Adapter<CourierWarehousesAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    interface OnItemClickCallBack {
        fun onItemClick(index: Int, isChecked: Boolean)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.courier_warehouse_item_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (id, name, fullAddress) = items[position]
        holder.binding.nameWarehouse.text = name
        holder.binding.fullAddressWarehouse.text = fullAddress
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItem(index: Int, item: CourierWarehousesItem) {
        if (items.size > index) items[index] = item
    }

    inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView),

        View.OnClickListener {
        var binding = CourierWarehouseItemLayoutBinding.bind(rootView)

        override fun onClick(v: View) {
            onItemClickCallBack.onItemClick(adapterPosition, false)
        }

        init {
            itemView.setOnClickListener(this)
        }
    }

}