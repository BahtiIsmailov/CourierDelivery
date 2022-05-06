package ru.wb.go.ui.courierloading

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.databinding.CourierLoadingDetailsLayoutBinding

class CourierLoadingDetailsAdapter(
    context: Context,
    private val addressItems: MutableList<CourierLoadingDetailsItem>
) : RecyclerView.Adapter<CourierLoadingDetailsAdapter.ViewHolder>() {
    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = inflater.inflate(R.layout.courier_loading_details_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (fullAddress, boxCount) = addressItems[position]
        holder.binding.fullAddressWarehouse.text = fullAddress
        holder.binding.boxGoals.text = boxCount
    }

    override fun getItemCount(): Int {
        return addressItems.size
    }

    inner class ViewHolder(rootView: View) : RecyclerView.ViewHolder(rootView) {
        var binding = CourierLoadingDetailsLayoutBinding.bind(rootView)
    }

    fun clear() {
        addressItems.clear()
    }

    fun addItems(addressItems: List<CourierLoadingDetailsItem>) {
        this.addressItems.addAll(addressItems)
    }

}