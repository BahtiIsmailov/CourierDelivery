package ru.wb.perevozka.ui.courierloading

import android.content.Context
import ru.wb.perevozka.R

class CourierLoadingResourceProvider(private val context: Context) {

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



    fun getIndexAndBarcode(index: Int, barcode: String): String =
        context.getString(R.string.dc_loading_boxes_index_barcode, index, barcode)

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

    fun getBoxDateAndTimeAndAddress(date: String, time: String, address: String): String =
        context.getString(R.string.courier_loading_boxes_date_and_time_and_address, date, time, address)

    fun getIndexWithQr(index: Int, qrcode : String) = context.getString(R.string.courier_loading_boxes_index, index, qrcode)

    fun getIndexUnnamedBarcode(index: Int, barcode: String): String {
        return context.getString(R.string.unnamed_barcode_format_number,
            index,
            barcode.take(4),
            barcode.takeLast(4))
    }


}