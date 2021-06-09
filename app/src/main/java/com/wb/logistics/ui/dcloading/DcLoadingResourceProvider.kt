package com.wb.logistics.ui.dcloading

import android.content.Context
import com.wb.logistics.R

class DcLoadingResourceProvider(private val context: Context) {

    fun getScanDialogTitle() = context.getString(R.string.dc_loading_scan_dialog_title_error)
    fun getScanDialogMessage() = context.getString(R.string.dc_loading_scan_dialog_message_error)
    fun getScanDialogButton() = context.getString(R.string.dc_loading_scan_dialog_positive_button_error)

    fun getShortAddedBox(code: String) : String = context.getString(R.string.dc_loading_code_short_added_box, code)
    fun getShortHasBeenAddedBox(code: String) : String = context.getString(R.string.dc_loading_code_short_has_been_added_box, code)
    fun getBoxNotBelongDcToolbarTitle() : String = context.getString(R.string.dc_loading_box_not_belong_dc_toolbar_title)
    fun getBoxNotBelongDcTitle() : String = context.getString(R.string.dc_loading_box_not_belong_dc_title)
    fun getBoxNotBelongFlightTitle() : String = context.getString(R.string.dc_loading_box_not_belong_flight_title)
    fun getBoxNotBelongFlightToolbarTitle() : String = context.getString(R.string.dc_loading_box_not_belong_flight_toolbar_title)
    fun getBoxNotBelongAddress() : String = context.getString(R.string.dc_loading_box_not_belong_address_title)

    fun getBoxDialogTitle() = context.getString(R.string.dc_loading_boxes_remove_dialog_title_error)
    fun getBoxDialogMessage() : String = context.getString(R.string.dc_loading_box_dialog_remove_error)
    fun getBoxesDialogMessage() : String = context.getString(R.string.dc_loading_boxes_dialog_remove_error)
    fun getBoxPositiveButton() = context.getString(R.string.dc_loading_boxes_remove_dialog_positive_button_error)


}