package ru.wb.go.ui.courierloading

import android.content.Context
import ru.wb.go.R

class CourierLoadingResourceProvider(private val context: Context) {

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



    fun getBoxDateAndTimeAndAddress(date: String, time: String, address: String): String =
        context.getString(R.string.courier_loading_boxes_date_and_time_and_address, date, time, address)

    fun getIndex(index: Int) = context.getString(R.string.courier_loading_boxes_index, index)

    fun getIndexUnnamedBarcode(index: Int, barcode: String): String {
        return context.getString(R.string.unnamed_barcode_format_number,
            index,
            barcode.take(4),
            barcode.takeLast(4))
    }

    fun getEmptyQr(): String = context.getString(R.string.courier_order_scanner_empty_qr)

    fun getEmptyAddress(): String = context.getString(R.string.courier_order_scanner_empty_address)

    fun getAccepted(count: Int): String = context.getString(R.string.courier_order_scanner_accepted, count)

    fun getGenericServiceTitleError() = context.getString(R.string.unknown_service_title_error)
    fun getGenericServiceMessageError() = context.getString(R.string.unknown_service_message_error)
    fun getGenericServiceButtonError() = context.getString(R.string.unknown_service_button_error)


}