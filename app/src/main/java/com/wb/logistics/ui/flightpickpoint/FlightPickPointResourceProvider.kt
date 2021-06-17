package com.wb.logistics.ui.flightpickpoint

import android.content.Context
import com.wb.logistics.R
import kotlin.math.abs

class FlightPickPointResourceProvider(private val context: Context) {

    fun getFlightToolbar(id: Int) =
        context.getString(R.string.flight_deliveries_flight_toolbar_title, id)

    fun getFlightNotDefineToolbar() =
        context.getString(R.string.flight_deliveries_flight_not_define_toolbar_title)

    fun getFlightListError() = context.getString(R.string.flight_deliveries_flight_list_error)

    fun getRedoCount(count: Int) = context.getString(R.string.flight_deliveries_redo_count, count)

    fun getUndoCount(count: Int) = context.getString(R.string.flight_deliveries_undo_count, count)

    fun getCountBox(count: Int) =
        context.resources.getQuantityString(R.plurals.reception_box_count, abs(count), count)

    fun getGenericError() = context.getString(R.string.unknown_generic_error)



}