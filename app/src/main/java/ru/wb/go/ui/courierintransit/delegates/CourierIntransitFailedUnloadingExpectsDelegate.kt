package ru.wb.go.ui.courierintransit.delegates

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.adapters.BaseAdapterDelegate
import ru.wb.go.databinding.CourierIntransitFailedUnloadingExpectsLayoutBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.courierintransit.delegates.items.CourierIntransitFaledUnloadingExpectsItem

class CourierIntransitFailedUnloadingExpectsDelegate(
    context: Context,
    private val onCourierIntransitCallback: OnCourierIntransitCallback
) : BaseAdapterDelegate<CourierIntransitFaledUnloadingExpectsItem, CourierIntransitFailedUnloadingExpectsDelegate.RouterViewHolder>(
    context
) {
    override fun isForViewType(item: BaseItem): Boolean {
        return item is CourierIntransitFaledUnloadingExpectsItem
    }

    override fun getLayoutId(): Int {
        return R.layout.courier_intransit_failed_unloading_expects_layout
    }

    override fun createViewHolder(view: View): RouterViewHolder {
        return RouterViewHolder(view)
    }

    override fun onBind(item: CourierIntransitFaledUnloadingExpectsItem, holder: RouterViewHolder) {
        holder.itemView.tag = item
        holder.binding.boxAddress.text = item.fullAddress
        holder.binding.deliveryCount.text = item.deliveryCount
        holder.binding.fromCount.text = item.fromCount
        holder.binding.selectedBackground.visibility =
            if (item.isSelected) View.VISIBLE else View.INVISIBLE
    }

    inner class RouterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = CourierIntransitFailedUnloadingExpectsLayoutBinding.bind(itemView)

        private fun getTag(itemView: View): CourierIntransitFaledUnloadingExpectsItem {
            return itemView.tag as CourierIntransitFaledUnloadingExpectsItem
        }

        init {
            binding.main.setOnClickListener {
                val (_, _, _, _, _, idView) = getTag(itemView)
                onCourierIntransitCallback.onPickToPointClick(idView)
            }
        }
    }
}