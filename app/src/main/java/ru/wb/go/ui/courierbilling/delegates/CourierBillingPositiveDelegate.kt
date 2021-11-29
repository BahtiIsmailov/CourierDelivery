package ru.wb.go.ui.courierbilling.delegates

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.adapters.BaseAdapterDelegate
import ru.wb.go.databinding.CourierBillingPositiveDelegateBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.courierbilling.delegates.items.CourierBillingPositiveItem

class CourierBillingPositiveDelegate(context: Context, val onCourierOrderCallback: OnCourierBillingCallback) :
    BaseAdapterDelegate<CourierBillingPositiveItem, CourierBillingPositiveDelegate.RouterViewHolder>(context) {


    override fun isForViewType(item: BaseItem): Boolean {
        return item is CourierBillingPositiveItem
    }

    override fun getLayoutId(): Int {
        return R.layout.courier_billing_positive_delegate
    }

    override fun createViewHolder(view: View): RouterViewHolder {
        return RouterViewHolder(view)
    }

    override fun onBind(item: CourierBillingPositiveItem, holder: RouterViewHolder) {
        holder.itemView.tag = item
        val binding = holder.binding
        binding.date.text = item.date
        binding.time.text = item.time
        binding.amount.text = item.amount
    }

    inner class RouterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding = CourierBillingPositiveDelegateBinding.bind(itemView)
        init {
            binding.background.setOnClickListener {
                onCourierOrderCallback.onOrderClick(getTag(itemView).idView)
            }
        }

        private fun getTag(itemView: View): CourierBillingPositiveItem {
            return itemView.tag as CourierBillingPositiveItem
        }
    }

}