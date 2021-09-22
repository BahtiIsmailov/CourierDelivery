package ru.wb.perevozka.ui.courierintransit.delegates

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.wb.perevozka.R
import ru.wb.perevozka.adapters.BaseAdapterDelegate
import ru.wb.perevozka.databinding.CourierIntransitEmptyLayoutBinding
import ru.wb.perevozka.mvvm.model.base.BaseItem
import ru.wb.perevozka.ui.courierintransit.delegates.items.CourierIntransitFailedItem

class CourierIntransitFaildDelegate(
    context: Context,
    private val onCourierIntransitCallback: OnCourierIntransitCallback
) : BaseAdapterDelegate<CourierIntransitFailedItem?, CourierIntransitFaildDelegate.RouterViewHolder?>(
    context
) {
    override fun isForViewType(item: BaseItem): Boolean {
        return item is CourierIntransitFailedItem
    }

    override fun getLayoutId(): Int {
        return R.layout.courier_intransit_faild_layout
    }

    override fun createViewHolder(view: View): RouterViewHolder {
        return RouterViewHolder(view)
    }

    override fun onBind(item: CourierIntransitFailedItem, holder: RouterViewHolder) {
        holder.itemView.tag = item
        holder.binding.address.text = item.fullAddress
        holder.binding.deliveryCount.text = item.deliveryCount
        holder.binding.fromCount.text = item.fromCount
        holder.binding.selectedBackground.visibility =
            if (item.isSelected) View.VISIBLE else View.INVISIBLE
    }

    inner class RouterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val binding = CourierIntransitEmptyLayoutBinding.bind(itemView)

        private fun getTag(itemView: View): CourierIntransitFailedItem {
            return itemView.tag as CourierIntransitFailedItem
        }

        init {
            binding.main.setOnClickListener { view: View ->
                val (_, _, _, _, _, idView) = getTag(itemView)
                onCourierIntransitCallback.onPickToPointClick(idView)
            }
        }
    }
}