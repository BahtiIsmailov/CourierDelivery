package com.wb.logistics.ui.dcunloading

import android.content.Context
import com.wb.logistics.R

class DcUnloadingScanResourceProvider(private val context: Context) {

    fun getBoxNotFoundTitle(): String =
        context.getString(R.string.dc_unloading_box_not_found_title)

    fun getBoxNotBelong(): String =
        context.getString(R.string.dc_unloading_box_not_belong)

    fun getBoxAlreadyUnloaded(barcode: String): String =
        context.getString(R.string.dc_unloading_box_already_unloaded, barcode)

    fun getBoxUnloaded(barcode: String): String =
        context.getString(R.string.dc_unloading_box_unloaded, barcode)

    fun getAccepted(returnCount: Int, fromTotal: Int) =
        context.getString(R.string.dc_unloading_box_unloaded_count, returnCount, fromTotal)

    fun getUnnamedBarcodeFormat(index: Int, barcode: String): String {
        return context.getString(R.string.unnamed_barcode_format,
            index,
            barcode.take(4),
            barcode.takeLast(4))
    }

    fun getScanDialogTitle() = context.getString(R.string.dc_loading_scan_dialog_title_return_error)
    fun getScanDialogMessage() =
        context.getString(R.string.dc_loading_scan_dialog_message_return_error)

    fun getScanDialogButton() =
        context.getString(R.string.dc_loading_scan_dialog_positive_button_return_error)

    fun getIndexAndBarcode(index: Int, barcode: String): String =
        context.getString(R.string.dc_loading_boxes_index_barcode, index, barcode)

    fun getBoxDateAndTime(date: String, time: String): String =
        context.getString(R.string.date_and_time_format, date, time)


}