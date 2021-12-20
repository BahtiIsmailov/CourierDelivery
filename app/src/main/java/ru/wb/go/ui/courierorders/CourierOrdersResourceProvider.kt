package ru.wb.go.ui.courierorders

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider
import kotlin.math.abs

class CourierOrdersResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getVersionApp(version: String) = context.getString(R.string.app_version, version)

    fun getArrive(arrive: String): String {
        return context.getString(R.string.courier_orders_confirm_arrive, arrive)
    }

    fun getBoxCountAndVolume(boxCount: Int, volume: Int): String {
        val v = context.resources.getQuantityString(R.plurals.volume, abs(volume), volume)
        return context.getString(R.string.courier_orders_count, boxCount, v)
    }

    fun getDialogTitle() = context.getString(R.string.courier_orders_confirm_dialog_title)
    fun getDialogMessage() = context.getString(R.string.courier_orders_confirm_dialog_message)
    fun getDialogButton() = context.getString(R.string.courier_orders_confirm_dialog_button)
    fun getDialogEmpty() = context.getString(R.string.courier_orders_confirm_dialog_empty)

}