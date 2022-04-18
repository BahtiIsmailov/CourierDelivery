package ru.wb.go.ui.courierloading

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseServicesResourceProvider

class CourierLoadingResourceProvider(private val context: Context) :
    BaseServicesResourceProvider(context) {

    fun getUnknown(): String = context.getString(R.string.courier_order_scanner_unknown_qr)

    fun getEmptyAddress(): String = context.getString(R.string.courier_order_scanner_empty_address)

    fun getAccepted(count: Int): String =
        context.getString(R.string.courier_order_scanner_accepted, count)

    fun getScanDialogTimeIsOutTitle() = context.getString(R.string.courier_order_scanner_title)
    fun getScanDialogTimeIsOutMessage() = context.getString(R.string.courier_order_scanner_message)
    fun getScanDialogTimeIsOutButton() = context.getString(R.string.courier_order_scanner_dialog)

}