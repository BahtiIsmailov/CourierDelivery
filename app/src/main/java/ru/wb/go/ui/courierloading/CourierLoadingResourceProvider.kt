package ru.wb.go.ui.courierloading

import android.content.Context
import ru.wb.go.R

class CourierLoadingResourceProvider(private val context: Context) {

    fun getScanDialogTitle() = context.getString(R.string.courier_unloading_dialog_title_error)
    fun getScanDialogMessage() = context.getString(R.string.courier_unloading_dialog_message_error)
    fun getScanDialogButton() = context.getString(R.string.courier_unloading_dialog_button_error)

    fun getBoxDialogTitle() = context.getString(R.string.dc_loading_boxes_remove_dialog_title_error)

    fun getErrorRemovedBoxesDialogMessage(): String =
        context.getString(R.string.dc_loading_boxes_dialog_remove_error)

    fun getBoxPositiveButton() =
        context.getString(R.string.dc_loading_boxes_remove_dialog_positive_button_error)



    fun getBoxDateAndTimeAndAddress(date: String, time: String, address: String): String =
        context.getString(R.string.courier_loading_boxes_date_and_time_and_address, date, time, address)

    fun getIndex(index: Int) = context.getString(R.string.courier_loading_boxes_index, index)

    fun getEmptyAddress(): String = context.getString(R.string.courier_order_scanner_empty_address)

    fun getAccepted(count: Int): String = context.getString(R.string.courier_order_scanner_accepted, count)

    fun getGenericServiceTitleError() = context.getString(R.string.unknown_service_title_error)
    fun getGenericServiceMessageError() = context.getString(R.string.unknown_service_message_error)
    fun getGenericServiceButtonError() = context.getString(R.string.unknown_service_button_error)


}