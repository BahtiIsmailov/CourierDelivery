package com.wb.logistics.ui.flights.delegates

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.wb.logistics.R
import com.wb.logistics.adapters.BaseAdapterDelegate
import com.wb.logistics.mvvm.model.base.BaseItem
import com.wb.logistics.ui.flights.delegates.items.FlightProgressItem

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