package com.wb.logistics.ui.flightdeliveries

import android.content.Context
import com.wb.logistics.R
import kotlin.math.abs

class FlightDeliveriesResourceProvider(private val context: Context) {

    fun getDeliveryToolbar(id: Int) =
        context.getString(R.string.flight_deliveries_delivery_toolbar_title, id)

    fun getRedoCount(count: Int) = context.getString(R.string.flight_deliveries_redo_count, count)

    fun getUndoCount(count: Int) = context.getString(R.string.flight_deliveries_undo_count, count)

    fun getEmptyCount(): String = context.getString(R.string.flight_deliveries_empty_count)

    fun getNotDelivery(delivery: Int, from: Int): String =
        context.getString(R.string.flight_deliveries_not_delivery, delivery, from)

    fun getDelivery(delivery: Int): String =
        context.getString(R.string.flight_deliveries_delivery, delivery)

    fun getReturnCount(count: Int): String =
        context.getString(R.string.flight_deliveries_return_count,
            count)

    fun getDescriptionDialog(count: Int): String =
        context.resources.getQuantityString(R.plurals.delivery_force_terminal_count,
            abs(count),
            count)

}