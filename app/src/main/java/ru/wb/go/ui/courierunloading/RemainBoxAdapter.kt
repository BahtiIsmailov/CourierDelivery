package ru.wb.go.ui.courierunloading

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.databinding.RemainBoxItemBinding
import ru.wb.go.db.entity.courierlocal.LocalBoxEntity

class RemainBoxAdapter(
    context: Context,
    private val items: MutableList<RemainBoxItem>,
    private val localBoxEntity: List<LocalBoxEntity>,
) : RecyclerView.Adapter<RemainBoxAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)
    private val resourceProvider = CourierUnloadingResourceProvider(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.remain_box_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.remainBoxCode.text = items[position].boxCode
        when (localBoxEntity[position].fakeDeliveredAt){
            null -> holder.binding.textForFailedBoxes.text = ""
            else-> {
                holder.binding.textForFailedBoxes.text = resourceProvider.getDescriptionInformationFailedBeepBox(
                    localBoxEntity[position].fakeDeliveredAt!!,
                    localBoxEntity[position].address
                )
                holder.binding.textForFailedBoxes.isVisible = true
            }
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class ViewHolder(rootView: View) :
        RecyclerView.ViewHolder(rootView) {

        var binding = RemainBoxItemBinding.bind(rootView)

    }

    fun clear() {
        items.clear()
    }

}