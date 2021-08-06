package ru.wb.perevozka.ui.flights.delegates

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import ru.wb.perevozka.R
import ru.wb.perevozka.databinding.FlightsRouteItemLayoutBinding

class FlightsAdapter(context: Context, private val items: List<String>) :
    ArrayAdapter<String>(context, R.layout.flights_route_item_layout) {

    override fun isEnabled(position: Int): Boolean {
        return false
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder
        if (convertView == null) {
            view =
                LayoutInflater.from(context).inflate(R.layout.flights_route_item_layout, null)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }
        holder.binding.routeLegendDescriptionText.text = items[position]
        return view
    }

    override fun getCount(): Int {
        return items.size
    }

    private inner class ViewHolder(rootView: View?) {
        var binding: FlightsRouteItemLayoutBinding = FlightsRouteItemLayoutBinding.bind(rootView!!)
    }

}