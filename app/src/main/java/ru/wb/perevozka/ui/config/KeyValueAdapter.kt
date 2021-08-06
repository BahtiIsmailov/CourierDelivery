package ru.wb.perevozka.ui.config

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.AdapterTwoRowItemLayoutBinding
import ru.wb.perevozka.ui.config.data.KeyValueDao

class KeyValueAdapter(context: Context?, val items: List<KeyValueDao>) :
    ArrayAdapter<KeyValueDao?>(
        context!!, R.layout.adapter_two_row_item_layout
    ) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView)
    }

    private fun getView(position: Int, convertView: View?): View {
        val view: View
        val keyValue = items[position]
        val holder: ViewHolder
        if (convertView == null) {
            view =
                LayoutInflater.from(context).inflate(R.layout.adapter_two_row_item_layout, null)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }
        holder.binding.text1.text = keyValue.key
        holder.binding.text2.text = keyValue.value
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        return getView(position, convertView)
    }

    override fun getItem(position: Int): KeyValueDao {
        return items[position]
    }

    override fun getCount(): Int {
        return items.size
    }

    private class ViewHolder(rootView: View) {

        var binding: AdapterTwoRowItemLayoutBinding

        init {
            binding = AdapterTwoRowItemLayoutBinding.bind(rootView)
        }
    }

}