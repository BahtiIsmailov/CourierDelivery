package ru.wb.perevozka.ui.courierorders.delegates

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.wb.perevozka.R
import ru.wb.perevozka.adapters.BaseAdapterDelegate
import ru.wb.perevozka.databinding.CourierOrderDelegateBinding
import ru.wb.perevozka.mvvm.model.base.BaseItem
import ru.wb.perevozka.ui.courierorders.delegates.items.CourierOrderItem
import ru.wb.perevozka.ui.flights.delegates.items.FlightItem

class CourierOrderDelegate(context: Context) :
    BaseAdapterDelegate<CourierOrderItem, CourierOrderDelegate.RouterViewHolder>(context) {

    override fun isForViewType(item: BaseItem): Boolean {
        return item is CourierOrderItem
    }

    override fun getLayoutId(): Int {
        return R.layout.courier_order_delegate
    }

    override fun createViewHolder(view: View): RouterViewHolder {
        return RouterViewHolder(view)
    }

    override fun onBind(item: CourierOrderItem, holder: RouterViewHolder) {
        holder.itemView.tag = item
        val binding = holder.binding
        binding.order.text = item.order
        binding.volume.text = item.volume
        binding.pvz.text = item.pvzCount
        binding.coast.text = item.coast
    }

    companion object {
        const val DURATION = 500L
    }

    inner class RouterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding = CourierOrderDelegateBinding.bind(itemView)
    }

}