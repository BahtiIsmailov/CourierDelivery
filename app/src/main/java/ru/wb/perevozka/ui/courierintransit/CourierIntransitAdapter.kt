package ru.wb.perevozka.ui.courierintransit

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.CourierIntransitDetailsLayoutBinding

class CourierIntransitAdapter(
    context: Context,
    private var items: MutableList<CourierIntransitItem>,
    private val onItemClickCallBack: OnItemClickCallBack,
) : RecyclerView.Adapter<CourierIntransitAdapter.ViewHolder>() {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    interface OnItemClickCallBack {
        fun onItemClick(index: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.courier_intransit_details_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (id, fullAddress, deliveryCount, isSelected) = items[position]
        holder.binding.address.text = fullAddress
        holder.binding.deliveryCount.text = deliveryCount
        holder.binding.selectedBackground.visibility =
            if (isSelected) View.VISIBLE else View.INVISIBLE
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItem(index: Int, item: CourierIntransitItem) {
        if (items.size > index) items[index] = item
    }

    fun setData(newItems: List<CourierIntransitItem>) {
        items = newItems as MutableList<CourierIntransitItem>
    }

    inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView),

        View.OnClickListener {
        var binding = CourierIntransitDetailsLayoutBinding.bind(rootView)

        override fun onClick(v: View) {
            onItemClickCallBack.onItemClick(adapterPosition)
        }

        init {
            itemView.setOnClickListener(this)
        }

    }

}