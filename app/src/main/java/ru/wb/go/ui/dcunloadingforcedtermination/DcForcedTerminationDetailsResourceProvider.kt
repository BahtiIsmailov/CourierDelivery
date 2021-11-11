package ru.wb.go.ui.dcunloadingforcedtermination

import android.content.Context
import ru.wb.go.R

class DcForcedTerminationDetailsResourceProvider(private val context: Context) {

    fun getNotDelivery(date: String, time: String) =
        context.getString(R.string.dc_unloading_forced_termination_details_delivery_data, date, time)

    fun getNotReturned(date: String, time: String, address: String) =
        context.getString(R.string.dc_unloading_forced_termination_details_return_data, date, time, address)

    fun getUnnamedBarcodeFormat(barcode: String): String {
        return context.getString(R.string.unnamed_barcode_format,
            barcode.take(4),
            barcode.takeLast(4))
    }

    fun getUnnamedBarcodeFormat(index: Int, barcode: String): String {
        return context.getString(R.string.unnamed_barcode_format_number,
            index,
            barcode.take(4),
            barcode.takeLast(4))
    }

}