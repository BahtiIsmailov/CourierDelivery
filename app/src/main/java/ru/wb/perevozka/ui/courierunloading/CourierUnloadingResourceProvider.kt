package ru.wb.perevozka.ui.courierunloading

import android.content.Context
import ru.wb.perevozka.R

class CourierUnloadingResourceProvider(private val context: Context) {

    fun getScanDialogTitle() = context.getString(R.string.courier_unloading_scan_dialog_title_error)
    fun getScanDialogMessage() =
        context.getString(R.string.courier_unloading_scan_dialog_message_error)

    fun getScanDialogButton() =
        context.getString(R.string.courier_uloading_scan_dialog_positive_button_error)

    fun getBoxDialogTitle() = context.getString(R.string.dc_loading_boxes_remove_dialog_title_error)

    fun getErrorRemovedBoxesDialogMessage(): String =
        context.getString(R.string.dc_loading_boxes_dialog_remove_error)

    fun getBoxPositiveButton() =
        context.getString(R.string.dc_loading_boxes_remove_dialog_positive_button_error)


    fun getBoxDateAndTimeAndAddress(date: String, time: String, address: String): String =
        context.getString(
            R.string.courier_loading_boxes_date_and_time_and_address,
            date,
            time,
            address
        )

    fun getIndex(index: Int) = context.getString(R.string.courier_loading_boxes_index, index)

    fun getReadyStatus(): String =
        context.getString(R.string.courier_unloading_scanner_ready_status)

    fun getReadyUnknownBox(): String =
        context.getString(R.string.courier_unloading_scanner_unknown_status)

    fun getReadyAddedBox(): String =
        context.getString(R.string.courier_unloading_scanner_added_status)

    fun getEmptyQr(): String = context.getString(R.string.courier_unloading_scanner_empty_qr)

    fun getEmptyAddress(): String =
        context.getString(R.string.courier_unloading_scanner_empty_address)

    fun getAccepted(deliveredCount: Int, fromCount: Int): String =
        context.getString(R.string.courier_unloading_scanner_accepted, deliveredCount, fromCount)

    fun getUnloadingDialogTitle(): String =
        context.getString(R.string.courier_unloading_dialog_complete_title)

    fun getUnloadingDialogMessage(deliveredCount: Int, fromCount: Int): String =
        context.getString(R.string.courier_unloading_dialog_message, deliveredCount, fromCount)

}