package ru.wb.go.ui.couriercarnumber

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.databinding.CourierCarTypeLayoutBinding

class CourierCarTypeAdapter(
    private val context: Context,
    private val typeItems: List<CourierCarTypeItem>,
    private val onItemClickCallBack: OnItemClickCallBack,
) : RecyclerView.Adapter<CourierCarTypeAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    interface OnItemClickCallBack {
        fun onItemClick(index: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.courier_car_type_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (icon, name) = typeItems[position]
        holder.binding.iconType.setImageDrawable(ContextCompat.getDrawable(context, icon))
        holder.binding.name.text = name
    }

    override fun getItemCount(): Int {
        return typeItems.size
    }

    inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView),
        View.OnClickListener {

        var binding = CourierCarTypeLayoutBinding.bind(rootView)

        override fun onClick(v: View) {
            onItemClickCallBack.onItemClick(adapterPosition)
        }

        init {
            itemView.setOnClickListener(this)
        }

    }

}