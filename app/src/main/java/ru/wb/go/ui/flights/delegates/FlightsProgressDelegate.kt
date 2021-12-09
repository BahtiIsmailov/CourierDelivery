package ru.wb.go.ui.flights.delegates

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import ru.wb.go.R
import ru.wb.go.adapters.BaseAdapterDelegate
import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.ui.flights.delegates.items.FlightProgressItem

class FlightsProgressDelegate(context: Context) :
    BaseAdapterDelegate<FlightProgressItem, FlightsProgressDelegate.ProgressViewHolder>(context) {

    override fun isForViewType(item: BaseItem): Boolean {
        return item is FlightProgressItem
    }

    override fun getLayoutId(): Int {
        return R.layout.flights_progress_delegate
    }

    override fun createViewHolder(view: View): ProgressViewHolder {
        return ProgressViewHolder(view)
    }

    override fun onBind(item: FlightProgressItem, holder: ProgressViewHolder) {}
    inner class ProgressViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView)
}