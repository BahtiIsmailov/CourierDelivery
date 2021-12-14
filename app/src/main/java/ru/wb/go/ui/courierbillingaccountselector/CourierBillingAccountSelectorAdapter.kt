package ru.wb.go.ui.courierbillingaccountselector

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import ru.wb.go.R

open class CourierBillingAccountSelectorAdapter(
    context: Context,
    val items: List<CourierBillingAccountSelectorAdapterItem>,
    val callback: OnCourierBillingAccountSelectorCallback
) : ArrayAdapter<CourierBillingAccountSelectorAdapterItem>(context, ID_LAYOUT, items) {

    companion object {
        private const val ID_LAYOUT = R.layout.billing_accounts_adapter_layout
    }

    override fun getView(position: Int, view: View?, parent: ViewGroup): View {
        var convertView = view
        val holder: ViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(ID_LAYOUT, null)
            holder = ViewHolder(convertView)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        when (val item = items[position]) {
            is CourierBillingAccountSelectorAdapterItem.Edit -> {
                holder.layoutAdd.visibility = View.GONE
                holder.layoutEdit.visibility = View.VISIBLE
                holder.textEdit.maxLines = 1
                holder.textEdit.text = item.shortText
            }
            is CourierBillingAccountSelectorAdapterItem.Add -> {
                holder.layoutAdd.visibility = View.VISIBLE
                holder.layoutEdit.visibility = View.GONE
                holder.textAdd.text = item.text
            }
        }
        return convertView!!
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(ID_LAYOUT, null, false)
            holder = ViewHolder(convertView)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        when (val item = items[position]) {
            is CourierBillingAccountSelectorAdapterItem.Edit -> {
                holder.layoutAdd.visibility = View.GONE
                holder.layoutEdit.visibility = View.VISIBLE
                holder.textEdit.maxLines = 2
                holder.textEdit.text = item.text
                holder.imageEdit.setOnClickListener { callback.onEditClick(position) }
            }
            is CourierBillingAccountSelectorAdapterItem.Add -> {
                holder.layoutAdd.visibility = View.VISIBLE
                holder.layoutEdit.visibility = View.GONE
                holder.textAdd.text = item.text
                holder.imageAdd.setOnClickListener { callback.onAddClick() }
            }
        }
        return convertView!!
    }

    override fun getItem(position: Int): CourierBillingAccountSelectorAdapterItem {
        return items[position]
    }

    override fun getCount(): Int {
        return items.size
    }

    protected class ViewHolder(rootView: View) {
        var layoutAdd: View = rootView.findViewById(R.id.add_layout)
        var imageAdd: ImageView = rootView.findViewById(R.id.image_add)
        var textAdd: TextView = rootView.findViewById(R.id.text_add)
        var layoutEdit: View = rootView.findViewById(R.id.edit_layout)
        var imageEdit: ImageView = rootView.findViewById(R.id.image_edit)
        var textEdit: TextView = rootView.findViewById(R.id.text_1)
    }

}