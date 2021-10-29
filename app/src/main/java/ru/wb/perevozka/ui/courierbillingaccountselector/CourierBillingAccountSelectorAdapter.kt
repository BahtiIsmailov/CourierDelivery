package ru.wb.perevozka.ui.courierbillingaccountselector

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import ru.wb.perevozka.R

class CourierBillingAccountSelectorAdapter(
    val context: Context,
    val items: List<String>,
    val callback: OnCourierBillingAccountSelectorCallback
) : BaseAdapter() {

    override fun getCount(): Int {
        return items.size
    }

    override fun getItem(p0: Int): Any {
        return items[p0]
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup?): View {
        val inflator = LayoutInflater.from(context)
        val convertView = inflator.inflate(
            if (position == items.size - 1) R.layout.billing_account_add_adapter_layout
            else R.layout.billing_account_adapter_layout,
            null
        )
        val text = convertView.findViewById<TextView>(R.id.text1)
        val icon = convertView.findViewById<ImageView>(R.id.image1)
        text.text = items[position]
        icon.setOnClickListener { callback.onEditClick(position) }

        return convertView
    }

}