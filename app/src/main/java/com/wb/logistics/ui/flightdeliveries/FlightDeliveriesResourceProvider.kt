package com.wb.logistics.ui.flightdeliveries

import android.content.Context
import com.wb.logistics.R
import kotlin.math.abs

class FlightDeliveriesResourceProvider(private val context: Context) {

    fun getCompleteDeliveryDialogTitle() = context.getString(R.string.flight_deliveries_dialog_title_error)
    fun getCompleteDeliveryDialogMessage() = context.getString(R.string.flight_deliveries_dialog_message_error)
    fun getCompleteDeliveryDialogButton() = context.getString(R.string.flight_deliveries_dialog_positive_button_error)

    fun getDeliveryToolbar(flightId: String) =
        context.getString(R.string.flight_deliveries_delivery_toolbar_title, flightId)

    fun getDeliveryToolbarEmpty() =
        context.getString(R.string.flight_deliveries_delivery_toolbar_empty_title)

    fun getDeliverCount(count: Int) = context.getString(R.string.flight_deliveries_deliver_count, count)

    fun getReturnCount(count: Int) = context.getString(R.string.flight_deliveries_pickup_count, count)

    fun getEmptyCount(): String = context.getString(R.string.flight_deliveries_empty_count)

    fun getNotDelivery(delivery: Int, from: Int): String =
        context.getString(R.string.flight_deliveries_not_delivery, delivery, from)

    fun getDelivery(delivery: Int): String =
        context.getString(R.string.flight_deliveries_delivery, delivery)

    fun getReturnedCount(count: Int): String =
        context.getString(R.string.flight_deliveries_return_count,
            count)

    fun getDescriptionDialog(count: Int): String =
        context.resources.getQuantityString(R.plurals.delivery_force_terminal_count,
            abs(count),
            count)

}