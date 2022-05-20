package ru.wb.go.ui.courierunloading

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseServicesResourceProvider

class CourierUnloadingResourceProvider(private val context: Context) :
    BaseServicesResourceProvider(context) {

    fun getScanDialogTitle() = context.getString(R.string.courier_unloading_dialog_title_error)
    fun getReadyStatus(): String =
        context.getString(R.string.courier_unloading_scanner_ready_status)

    fun getReadyWrongBox(): String =
        context.getString(R.string.courier_unloading_scanner_unknown_status)

    fun getReadyForbiddenBox(): String ="КОРОБКА НЕ ПРИНИМАЛАСЬ"

    fun getReadyAddedBox(): String =
        context.getString(R.string.courier_unloading_scanner_added_status)

    fun getEmptyQr(): String = context.getString(R.string.courier_unloading_scanner_empty_qr)
    fun getUnknownQr(): String = context.getString(R.string.courier_order_scanner_unknown_qr)

    fun getEmptyAddress(): String =
        context.getString(R.string.courier_unloading_scanner_empty_address)

    fun getAccepted(deliveredCount: Int, fromCount: Int): String =
        context.getString(R.string.courier_unloading_scanner_accepted, deliveredCount, fromCount)

    fun getUnloadingDialogTitle(): String =
        context.getString(R.string.courier_unloading_dialog_complete_title)

    fun getUnloadingDialogMessage(deliveredCount: Int, fromCount: Int): String =
        context.getString(R.string.courier_unloading_dialog_message, deliveredCount, fromCount)

    fun getUnloadingDetails(index: Int, lastNumber: String): String =
        context.getString(R.string.courier_unloading_details, index, lastNumber)

    fun getUnloadingDialogPositive(): String =
        context.getString(R.string.courier_order_scanner_dialog_positive_button)

    fun getUnloadingDialogNegative(): String =
        context.getString(R.string.courier_order_scanner_dialog_negative_button)

    fun getOrderId(id : String): String {
        return context.getString(R.string.courier_intransit_label_id, id)
    }

}