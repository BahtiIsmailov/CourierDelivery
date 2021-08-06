package ru.wb.perevozka.ui.flightpickpoint

import android.content.Context
import ru.wb.perevozka.R
import kotlin.math.abs

class FlightPickPointResourceProvider(private val context: Context) {

    fun getFlightToolbar(id: Int) =
        context.getString(R.string.flight_deliveries_flight_toolbar_title, id)

    fun getFlightNotDefineToolbar() =
        context.getString(R.string.flight_deliveries_flight_not_define_toolbar_title)

    fun getFlightListError() = context.getString(R.string.flight_deliveries_flight_list_error)

    fun getDeliverCount(count: Int) = context.getString(R.string.flight_deliveries_deliver_count, count)

    fun getPickupCount(count: Int) = context.getString(R.string.flight_deliveries_pickup_count, count)

    fun getCountBox(count: Int) =
        context.resources.getQuantityString(R.plurals.reception_box_count, abs(count), count)

    fun getGenericError() = context.getString(R.string.unknown_generic_error)



}