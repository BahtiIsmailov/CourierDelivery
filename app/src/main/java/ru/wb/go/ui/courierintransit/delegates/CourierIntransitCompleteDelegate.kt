package ru.wb.go.ui.courierintransit.delegates

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.adapters.BaseAdapterDelegate
import ru.wb.go.databinding.CourierIntransitDelegateEmptyLayoutBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.courierintransit.delegates.items.CourierIntransitCompleteItem

class CourierIntransitCompleteDelegate(
    context: Context,
    private val onCourierIntransitCallback: OnCourierIntransitCallback
) : BaseAdapterDelegate<CourierIntransitCompleteItem, CourierIntransitCompleteDelegate.RouterViewHolder>(
    context
) {
    override fun isForViewType(item: BaseItem): Boolean {
        return item is CourierIntransitCompleteItem
    }

    override fun getLayoutId(): Int {
        return R.layout.courier_intransit_delegate_complete_layout
    }

    override fun createViewHolder(view: View): RouterViewHolder {
        return RouterViewHolder(view)
    }

    override fun onBind(item: CourierIntransitCompleteItem, holder: RouterViewHolder) {
        holder.itemView.tag = item
        holder.binding.boxAddress.text = item.fullAddress
        holder.binding.timeWorkDetail12.text = item.timeWork
        holder.binding.deliveryCount.text = item.deliveryCount
        holder.binding.fromCount.text = item.fromCount
        val selectable = if (item.isSelected) View.VISIBLE else View.INVISIBLE
        holder.binding.selectedBackground.visibility = selectable
        holder.binding.imageItemBorder.visibility = selectable
    }

    inner class RouterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = CourierIntransitDelegateEmptyLayoutBinding.bind(itemView)

        private fun getTag(itemView: View): CourierIntransitCompleteItem {
            return itemView.tag as CourierIntransitCompleteItem
        }

        init {
            binding.main.setOnClickListener {
                val (_, _, _, _, _, _, idView) = getTag(itemView)
                onCourierIntransitCallback.onPickToPointClick(idView)
            }
        }
    }
}