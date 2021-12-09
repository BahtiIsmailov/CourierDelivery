package ru.wb.go.ui.courierintransit.delegates

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.adapters.BaseAdapterDelegate
import ru.wb.go.databinding.CourierIntransitFailedUnloadingAllLayoutBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.courierintransit.delegates.items.CourierIntransitFailedUnloadingAllItem

class CourierIntransitFailedUnloadingAllDelegate(
    context: Context,
    private val onCourierIntransitCallback: OnCourierIntransitCallback
) : BaseAdapterDelegate<CourierIntransitFailedUnloadingAllItem, CourierIntransitFailedUnloadingAllDelegate.RouterViewHolder>(
    context
) {
    override fun isForViewType(item: BaseItem): Boolean {
        return item is CourierIntransitFailedUnloadingAllItem
    }

    override fun getLayoutId(): Int {
        return R.layout.courier_intransit_failed_unloading_all_layout
    }

    override fun createViewHolder(view: View): RouterViewHolder {
        return RouterViewHolder(view)
    }

    override fun onBind(unloadingAllItem: CourierIntransitFailedUnloadingAllItem, holder: RouterViewHolder) {
        holder.itemView.tag = unloadingAllItem
        holder.binding.address.text = unloadingAllItem.fullAddress
        holder.binding.deliveryCount.text = unloadingAllItem.deliveryCount
        holder.binding.fromCount.text = unloadingAllItem.fromCount
        holder.binding.selectedBackground.visibility =
            if (unloadingAllItem.isSelected) View.VISIBLE else View.INVISIBLE
    }

    inner class RouterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = CourierIntransitFailedUnloadingAllLayoutBinding.bind(itemView)

        private fun getTag(itemView: View): CourierIntransitFailedUnloadingAllItem {
            return itemView.tag as CourierIntransitFailedUnloadingAllItem
        }

        init {
            binding.main.setOnClickListener { view: View ->
                val (_, _, _, _, _, idView) = getTag(itemView)
                onCourierIntransitCallback.onPickToPointClick(idView)
            }
        }
    }
}