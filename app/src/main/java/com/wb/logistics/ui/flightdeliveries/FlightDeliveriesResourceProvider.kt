package com.wb.logistics.ui.flightdeliveries

import android.content.Context
import com.wb.logistics.R
import kotlin.math.abs

class FlightDeliveriesResourceProvider(private val context: Context) {

    fun getFlightToolbar(id: Int): String =
        context.getString(R.string.flight_deliveries_flight_toolbar_title,
            id)

    fun getDeliveryToolbar(id: Int): String =
        context.getString(R.string.flight_deliveries_delivery_toolbar_title,
            id)

    fun getEmpty(): String = context.getString(R.string.flight_deliveries_title_empty)

    fun getRedoCount(count: Int): String = context.getString(R.string.flight_deliveries_redo_count,
        count)

    fun getUndoCount(count: Int): String = context.getString(R.string.flight_deliveries_undo_count,
        count)

    fun getEmptyCount(): String = context.getString(R.string.flight_deliveries_empty_count)

    fun getCountBox(count: Int): String = context.resources
        .getQuantityString(R.plurals.reception_box_count, abs(count), count)

}