package com.wb.logistics.ui.unloading

import android.content.Context
import com.wb.logistics.R

class UnloadingScanResourceProvider(private val context: Context) {

    fun getNumericBarcode(number: Int, barcode: String): String =
        context.getString(R.string.unloading_boxes_numeric_barcode, number, barcode)

    fun getBoxNotBelongPointToolbarTitle(): String =
        context.getString(R.string.unloading_box_not_belong_point_toolbar_label)

    fun getBoxNotBelongPvzTitle(): String =
        context.getString(R.string.unloading_box_not_belong_dc_title)

    fun getBoxNotBelongPvzDescription(): String =
        context.getString(R.string.unloading_box_not_belong_dc_return_car)

    fun getOfficeEmpty(officeId: Int) =
        context.getString(R.string.unloading_boxes_office_empty, officeId)

    fun getHandleFormatBox(index: Int, suffix: String, postfix: String) =
        context.getString(R.string.unloading_handle_box_format, index, suffix, postfix)

    fun getAlreadyReturned(barcode: String) =
        context.getString(R.string.unloading_box_already_returned, barcode)

    fun getAlreadyDelivery(barcode: String) =
        context.getString(R.string.unloading_box_already_delivery, barcode)

    fun getReturned(barcode: String) =
        context.getString(R.string.unloading_box_returned, barcode)

    fun getDelivered(barcode: String) =
        context.getString(R.string.unloading_box_delivered, barcode)

    fun getBoxNotBelongTitle(): String =
        context.getString(R.string.unloading_box_not_belong_dc_title)

    fun getBoxNotBelongInfoTitle(): String =
        context.getString(R.string.unloading_box_not_belong_info_title)

    fun getBoxEmptyInfoDescription(): String =
        context.getString(R.string.unloading_box_not_belong_info_empty_description)

    fun getBoxNotBelongAddress(): String =
        context.getString(R.string.dc_loading_box_not_belong_address_title)

    fun getScanDialogTitle() = context.getString(R.string.dc_loading_scan_dialog_title_error)
    fun getScanDialogMessage() = context.getString(R.string.dc_loading_scan_dialog_message_error)
    fun getScanDialogButton() =
        context.getString(R.string.dc_loading_scan_dialog_positive_button_error)

}