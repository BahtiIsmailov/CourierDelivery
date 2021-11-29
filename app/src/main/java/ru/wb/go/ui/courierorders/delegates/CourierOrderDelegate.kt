package ru.wb.go.ui.courierorders.delegates

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.adapters.BaseAdapterDelegate
import ru.wb.go.databinding.CourierOrderDelegateBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.courierorders.delegates.items.CourierOrderItem

class CourierOrderDelegate(context: Context, val onCourierOrderCallback: OnCourierOrderCallback) :
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
        binding.arrive.text = item.arrive
        binding.volume.text = item.volume
        binding.pvz.text = item.pvzCount
        binding.coast.text = item.coast
    }

    inner class RouterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding = CourierOrderDelegateBinding.bind(itemView)

        init {
            binding.background.setOnClickListener {
                onCourierOrderCallback.onOrderClick(getTag(itemView).idView)
            }
        }

        private fun getTag(itemView: View): CourierOrderItem {
            return itemView.tag as CourierOrderItem
        }
    }

}