package ru.wb.go.ui.courierbilling.delegates

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.adapters.BaseAdapterDelegate
import ru.wb.go.databinding.CourierBillingPositiveDelegateBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.courierbilling.delegates.items.CourierBillingPositiveItem

class CourierBillingPositiveDelegate(
    context: Context,
) :
    BaseAdapterDelegate<CourierBillingPositiveItem, CourierBillingPositiveDelegate.RouterViewHolder>(
        context
    ) {


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
        with(holder.binding) {
            date.text = item.date
            mapTimer.text = item.time
            amount.text = item.amount
            statusDescription.text = "Оплата заказа"
        }
    }

    inner class RouterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var binding = CourierBillingPositiveDelegateBinding.bind(itemView)

    }

}