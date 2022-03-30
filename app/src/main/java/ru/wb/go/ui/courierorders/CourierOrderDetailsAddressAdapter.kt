package ru.wb.go.ui.courierorders

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.databinding.CourierOrderDetailsLayoutBinding

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
        val view = inflater.inflate(R.layout.courier_order_details_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (fullAddress) = addressItems[position]
        holder.binding.fullAddressWarehouse.text = fullAddress
    }

    override fun getItemCount(): Int {
        return addressItems.size
    }

    fun setItem(index: Int, addressItem: CourierOrderDetailsAddressItem) {
        if (addressItems.size > index) addressItems[index] = addressItem
    }

    inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView),

        View.OnClickListener {
        var binding = CourierOrderDetailsLayoutBinding.bind(rootView)

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