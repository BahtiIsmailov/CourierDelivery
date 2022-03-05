package ru.wb.go.ui.courierintransit.delegates

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.adapters.BaseAdapterDelegate
import ru.wb.go.databinding.CourierIntransitUnloadingExpectsLayoutBinding
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.courierintransit.delegates.items.CourierIntransitUnloadingExpectsItem

class CourierIntransitUnloadingExpectsDelegate(
    context: Context,
    private val onCourierIntransitCallback: OnCourierIntransitCallback
) : BaseAdapterDelegate<CourierIntransitUnloadingExpectsItem, CourierIntransitUnloadingExpectsDelegate.RouterViewHolder>(
    context
) {
    override fun isForViewType(item: BaseItem): Boolean {
        return item is CourierIntransitUnloadingExpectsItem
    }

    override fun getLayoutId(): Int {
        return R.layout.courier_intransit_unloading_expects_layout
    }

    override fun createViewHolder(view: View): RouterViewHolder {
        return RouterViewHolder(view)
    }

    override fun onBind(item: CourierIntransitUnloadingExpectsItem, holder: RouterViewHolder) {
        holder.itemView.tag = item
        holder.binding.boxAddress.text = item.fullAddress
        holder.binding.deliveryCount.text = item.deliveryCount
        holder.binding.fromCount.text = item.fromCount
        val selectable = if (item.isSelected) View.VISIBLE else View.INVISIBLE
        holder.binding.selectedBackground.visibility = selectable
        holder.binding.imageItemBorder.visibility = selectable
    }

    inner class RouterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = CourierIntransitUnloadingExpectsLayoutBinding.bind(itemView)

        private fun getTag(itemView: View): CourierIntransitUnloadingExpectsItem {
            return itemView.tag as CourierIntransitUnloadingExpectsItem
        }

        init {
            binding.main.setOnClickListener {
                val (_, _, _, _, _, idView) = getTag(itemView)
                onCourierIntransitCallback.onPickToPointClick(idView)
            }
        }
    }
}