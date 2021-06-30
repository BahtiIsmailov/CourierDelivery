package com.wb.logistics.ui.dcloading

import android.content.Context
import com.wb.logistics.R

class DcLoadingResourceProvider(private val context: Context) {

    fun getScanDialogTitle() = context.getString(R.string.dc_loading_scan_dialog_title_error)
    fun getScanDialogMessage() = context.getString(R.string.dc_loading_scan_dialog_message_error)
    fun getScanDialogButton() =
        context.getString(R.string.dc_loading_scan_dialog_positive_button_error)

    fun getSwitchDialogButton() =
        context.getString(R.string.dc_loading_scan_switch_dialog_message_error)


    fun getShortHasBeenAddedBox(code: String): String =
        context.getString(R.string.dc_loading_code_short_has_been_added_box, code)

    fun getBoxTimeAndAddress(time: String, address: String): String =
        context.getString(R.string.dc_loading_boxes_time, time, address)

    fun getBoxNotBelongDcTitle(): String =
        context.getString(R.string.dc_loading_box_not_belong_dc_title)

    fun getBoxNotBelongFlightTitle(): String =
        context.getString(R.string.dc_loading_box_not_belong_flight_title)

    fun getBoxNotBelongInfoTitle(): String =
        context.getString(R.string.dc_loading_box_not_belong_info_title)

    fun getBoxNotBelongAddress(): String =
        context.getString(R.string.dc_loading_box_not_belong_address_title)

    fun getBoxDialogTitle() = context.getString(R.string.dc_loading_boxes_remove_dialog_title_error)
    fun getErrorRemovedBoxesDialogMessage(): String =
        context.getString(R.string.dc_loading_boxes_dialog_remove_error)

    fun getBoxPositiveButton() =
        context.getString(R.string.dc_loading_boxes_remove_dialog_positive_button_error)

    fun getEmptyGate(): String = context.getString(R.string.dc_loading_box_not_belong_empty_gate)

    fun getIndex(index: Int) = context.getString(R.string.dc_loading_boxes_index, index)


}