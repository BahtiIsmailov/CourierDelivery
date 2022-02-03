package ru.wb.go.ui.courierbilling.delegates

import android.content.Context
import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.adapters.BaseAdapterDelegate
import ru.wb.go.databinding.CourierBillingNegativeDelegateBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.courierbilling.delegates.items.CourierBillingNegativeItem

class CourierBillingNegativeDelegate(
    context: Context
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
        with(holder.binding) {
            date.text = item.date
            mapTimer.text = item.time
            amount.text = item.amount
            if(item.statusIcon!=null) {
                icStatus.setImageDrawable(ContextCompat.getDrawable(context, item.statusIcon))
            }
            statusDescription.text = item.statusDescription
        }
    }

    inner class RouterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = CourierBillingNegativeDelegateBinding.bind(itemView)

    }

}