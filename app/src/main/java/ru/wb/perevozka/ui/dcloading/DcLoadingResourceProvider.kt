package ru.wb.perevozka.ui.dcloading

import android.content.Context
import ru.wb.perevozka.R

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

    fun getBoxDateAndTime(date: String, time: String): String =
        context.getString(R.string.date_and_time_format, date, time)

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

    fun getIndex(index: Int) = context.getString(R.string.dc_loading_boxes_index, index)

    fun getIndexUnnamedBarcode(index: Int, barcode: String): String {
        return context.getString(R.string.unnamed_barcode_format_number,
            index,
            barcode.take(4),
            barcode.takeLast(4))
    }


}