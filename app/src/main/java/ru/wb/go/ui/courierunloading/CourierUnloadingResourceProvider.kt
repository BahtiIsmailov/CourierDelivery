package ru.wb.go.ui.courierunloading

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider

class CourierUnloadingResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getScanDialogTitle() = context.getString(R.string.courier_unloading_dialog_title_error)
    fun getScanDialogMessage() = context.getString(R.string.courier_unloading_dialog_message_error)
    fun getScanDialogButton() = context.getString(R.string.courier_unloading_dialog_button_error)



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

    fun getUnloadingDialogPositive(): String =
        context.getString(R.string.courier_order_scanner_dialog_positive_button)

    fun getUnloadingDialogNegative(): String =
        context.getString(R.string.courier_order_scanner_dialog_negative_button)

}