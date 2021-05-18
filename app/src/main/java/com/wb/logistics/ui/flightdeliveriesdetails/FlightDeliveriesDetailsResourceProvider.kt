package com.wb.logistics.ui.flightdeliveriesdetails

import android.content.Context
import com.wb.logistics.R

class FlightDeliveriesDetailsResourceProvider(private val context: Context) {

    fun getDeliveryDate(date: String, time: String) =
        context.getString(R.string.force_termination_not_delivery_data, date, time)

    fun getReturnDate(date: String, time: String, address: String) =
        context.getString(R.string.force_termination_not_return_data, date, time, address)

    fun getDeliveryTitle(): String =
        context.getString(R.string.flight_deliveries_details_delivery_title)

    fun getReturnTitle(): String =
        context.getString(R.string.flight_deliveries_details_return_title)

    fun getCountTitle(count: Int): String =
        context.getString(R.string.flight_deliveries_details_title_count, count)


}