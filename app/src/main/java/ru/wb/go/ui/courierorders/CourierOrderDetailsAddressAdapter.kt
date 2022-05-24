package ru.wb.go.ui.courierorders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.databinding.CourierOrdersAddressLayoutBinding

class CourierOrderDetailsAddressAdapter(
    context: Context,
    private val addressItems: MutableList<CourierOrderDetailsAddressItem>,
    private val onItemClickCallBack: OnItemClickCallBack,
) : RecyclerView.Adapter<CourierOrderDetailsAddressAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    interface OnItemClickCallBack {
        fun onItemClick(index: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.courier_orders_address_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (fullAddress, isSelected) = addressItems[position]
        holder.binding.fullAddressWarehouse.text = fullAddress
        val selectable = if (isSelected) View.VISIBLE else View.INVISIBLE
        holder.binding.selectedBackground.visibility = selectable
        holder.binding.imageItemBorder.visibility = selectable
    }

    override fun getItemCount(): Int {
        return addressItems.size
    }

    inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView),
        View.OnClickListener {

        var binding = CourierOrdersAddressLayoutBinding.bind(rootView)

        override fun onClick(v: View) {
            onItemClickCallBack.onItemClick(adapterPosition)
        }

        init {
            itemView.setOnClickListener(this)
        }

    }

    fun clear() {
        addressItems.clear()
    }

    fun addItems(addressItems: List<CourierOrderDetailsAddressItem>) {
        this.addressItems.addAll(addressItems)
    }

}