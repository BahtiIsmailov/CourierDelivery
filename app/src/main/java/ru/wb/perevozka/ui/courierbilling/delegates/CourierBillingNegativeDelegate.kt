package ru.wb.perevozka.ui.courierbilling.delegates

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.wb.perevozka.R
import ru.wb.perevozka.adapters.BaseAdapterDelegate
import ru.wb.perevozka.databinding.CourierBillingNegativeDelegateBinding
import ru.wb.perevozka.mvvm.model.base.BaseItem
import ru.wb.perevozka.ui.courierbilling.delegates.items.CourierBillingNegativeItem
import ru.wb.perevozka.ui.courierbilling.delegates.items.CourierBillingPositiveItem

class CourierBillingNegativeDelegate(
    context: Context,
    val onCourierOrderCallback: OnCourierBillingCallback
) :
    BaseAdapterDelegate<CourierBillingNegativeItem, CourierBillingNegativeDelegate.RouterViewHolder>(
        context
    ) {


    override fun isForViewType(item: BaseItem): Boolean {
        return item is CourierBillingNegativeItem
    }

    override fun getLayoutId(): Int {
        return R.layout.courier_billing_negative_delegate
    }

    override fun createViewHolder(view: View): RouterViewHolder {
        return RouterViewHolder(view)
    }

    override fun onBind(item: CourierBillingNegativeItem, holder: RouterViewHolder) {
        holder.itemView.tag = item
        val binding = holder.binding
        binding.date.text = item.date
        binding.time.text = item.time
        binding.amount.text = item.amount
    }

    inner class RouterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding = CourierBillingNegativeDelegateBinding.bind(itemView)

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