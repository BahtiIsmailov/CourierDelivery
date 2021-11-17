package ru.wb.go.ui.flightdeliveriesdetails

import android.content.Context
import ru.wb.go.R

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

    fun getNotFoundOnUnloading(date: String, time: String) =
        context.getString(R.string.force_termination_info_empty_data, date, time)

    fun getTriedToUnload(date: String, time: String, address: String) =
        context.getString(R.string.force_termination_not_belong_data, date, time, address)

    fun getUnnamedBarcodeFormat(barcode: String): String {
        return context.getString(R.string.unnamed_barcode_format,
            barcode.take(4),
            barcode.takeLast(4))
    }

}