package ru.wb.go.ui.courierwarehouses.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.databinding.AddressDetailLayoutItemBinding
import ru.wb.go.ui.courierorders.CourierOrderDetailsAddressItem

class CourierListAddressesItemAdapter : RecyclerView.Adapter<CourierListAddressesItemAdapter.CustomViewHolder>() {
    var items: MutableSet<CourierOrderDetailsAddressItem> = mutableSetOf()

    class CustomViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var binding = AddressDetailLayoutItemBinding.bind(itemView)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.address_detail_layout_item, parent, false)
        return CustomViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        val binding = holder.binding
        with(binding) {
             addressDetail.text = items.elementAt(position).fullAddress
             iconAddress.setImageResource(
                 if (items.elementAt(position).isUnspentTimeWork){
                     R.drawable.ic_order_item_point
                 }else{
                     R.drawable.ic_address_point_normal
                 }
             )
             timeWorkDetail.text = items.elementAt(position).timeWork
        }
    }

    fun addItems(itemsFormFragment:MutableSet<CourierOrderDetailsAddressItem>){
         items = itemsFormFragment
    }

    fun clear(){
        this.items.toMutableList().clear()
    }
    override fun getItemCount(): Int = items.size
}