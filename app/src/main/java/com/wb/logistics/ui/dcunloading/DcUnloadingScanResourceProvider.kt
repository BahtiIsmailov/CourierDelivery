package com.wb.logistics.ui.dcunloading

import android.content.Context
import com.wb.logistics.R

class DcUnloadingScanResourceProvider(private val context: Context) {

    fun getBoxNotFoundTitle(): String =
        context.getString(R.string.dc_unloading_box_not_found_title)

    fun getBoxAlreadyUnloaded(barcode: String): String =
        context.getString(R.string.dc_unloading_box_already_unloaded, barcode)

    fun getBoxUnloaded(barcode: String): String =
        context.getString(R.string.dc_unloading_box_unloaded, barcode)

    fun getBoxUnloadedCount(returnCount: Int, fromCount: Int) =
        context.getString(R.string.dc_unloading_box_unloaded_count, returnCount, fromCount)

    fun getScanDialogTitle() = context.getString(R.string.dc_loading_scan_dialog_title_return_error)
    fun getScanDialogMessage() = context.getString(R.string.dc_loading_scan_dialog_message_return_error)
    fun getScanDialogButton() =
        context.getString(R.string.dc_loading_scan_dialog_positive_button_return_error)


}