package ru.wb.go.ui.courierintransit.delegates

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.adapters.BaseAdapterDelegate
import ru.wb.go.databinding.CourierIntransitDelegateEmptyLayoutBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.courierintransit.delegates.items.CourierIntransitEmptyItem

class CourierIntransitEmptyDelegate(
    context: Context,
    private val onCourierIntransitCallback: OnCourierIntransitCallback
) : BaseAdapterDelegate<CourierIntransitEmptyItem, CourierIntransitEmptyDelegate.RouterViewHolder>(
    context
) {
    override fun isForViewType(item: BaseItem): Boolean {
        return item is CourierIntransitEmptyItem
    }

    override fun getLayoutId(): Int {
        return R.layout.courier_intransit_delegate_empty_layout
    }

    override fun createViewHolder(view: View): RouterViewHolder {
        return RouterViewHolder(view)
    }

    override fun onBind(item: CourierIntransitEmptyItem, holder: RouterViewHolder) {
        holder.itemView.tag = item
        holder.binding.boxAddress.text = item.fullAddress
        holder.binding.deliveryCount.text = item.deliveryCount
        holder.binding.fromCount.text = item.fromCount
        val selectable = if (item.isSelected) View.VISIBLE else View.INVISIBLE
        holder.binding.selectedBackground.visibility = selectable
        holder.binding.imageItemBorder.visibility = selectable
    }

    inner class RouterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val binding = CourierIntransitDelegateEmptyLayoutBinding.bind(itemView)

        private fun getTag(itemView: View): CourierIntransitEmptyItem {
            return itemView.tag as CourierIntransitEmptyItem
        }

        init {
            binding.main.setOnClickListener {
                val (_, _, _, _, _, idView) = getTag(itemView)
                onCourierIntransitCallback.onPickToPointClick(idView)
            }
        }
    }
}