package com.wb.logistics.ui.unloadingscan

import android.content.Context
import com.wb.logistics.R

class UnloadingScanResourceProvider(private val context: Context) {

    fun getNumericBarcode(number: Int, barcode: String): String =
        context.getString(R.string.unloading_boxes_numeric_barcode, number, barcode)

    fun getBoxNotBelongPvzTitle(): String =
        context.getString(R.string.unloading_box_not_belong_dc_title)

    fun getBoxNotBelongPvzDescription(): String =
        context.getString(R.string.unloading_box_not_belong_dc_return_car)

    fun getOfficeEmpty(officeId: Int) =
        context.getString(R.string.unloading_boxes_office_empty, officeId)

    fun getHandleFormatBox(index: Int, suffix: String, postfix: String) =
        context.getString(R.string.unnamed_barcode_format_number, index, suffix, postfix)

    fun getBoxNotBelongTitle(): String =
        context.getString(R.string.unloading_box_not_belong_dc_title)

    fun getBoxNotBelongInfoTitle(): String =
        context.getString(R.string.unloading_box_not_belong_info_title)

    fun getBoxEmptyInfoDescription(): String =
        context.getString(R.string.unloading_box_not_belong_info_empty_description)

    fun getBoxNotInfoAddress(): String =
        context.getString(R.string.dc_loading_box_not_belong_address_title)

    fun getAccepted(count: Int, total: Int) = context.getString(R.string.unloading_box_accepted, count, total)

    fun getBoxTimeAndTime(date: String, time: String): String =
        context.getString(R.string.date_and_time_format, date, time)

    fun getScanDialogTitle() = context.getString(R.string.unloading_scan_dialog_title_error)
    fun getScanDialogMessage() = context.getString(R.string.unloading_scan_dialog_message_error)
    fun getScanDialogButton() =
        context.getString(R.string.unloading_scan_dialog_positive_button_error)

    fun getUnnamedBarcodeFormat(index: Int, barcode: String): String {
        return context.getString(R.string.unnamed_barcode_format_number,
            index,
            barcode.take(4),
            barcode.takeLast(4))
    }

}