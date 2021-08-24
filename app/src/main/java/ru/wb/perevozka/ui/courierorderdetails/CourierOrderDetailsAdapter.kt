package ru.wb.perevozka.ui.courierorderdetails

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.CourierOrderDetailsLayoutBinding

class CourierOrderDetailsAdapter(
    context: Context,
    private val items: MutableList<CourierOrderDetailsItem>,
    private val onItemClickCallBack: OnItemClickCallBack,
) : RecyclerView.Adapter<CourierOrderDetailsAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    interface OnItemClickCallBack {
        fun onItemClick(index: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.courier_order_details_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (id, fullAddress) = items[position]
        holder.binding.fullAddressWarehouse.text = fullAddress
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setItem(index: Int, item: CourierOrderDetailsItem) {
        if (items.size > index) items[index] = item
    }

    inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView),

        View.OnClickListener {
        var binding = CourierOrderDetailsLayoutBinding.bind(rootView)

        override fun onClick(v: View) {
            onItemClickCallBack.onItemClick(adapterPosition)
        }

        init {
            itemView.setOnClickListener(this)
        }

    }

}