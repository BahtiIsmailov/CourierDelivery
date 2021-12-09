package ru.wb.go.ui.courierorders

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider
import kotlin.math.abs

class CourierOrdersResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getErrorOrderDialogTitle() = context.getString(R.string.courier_order_dialog_title_error)

    fun getErrorOrderDialogMessage(): String =
        context.getString(R.string.courier_order_dialog_error)

    fun getErrorOrderDialogPositiveButton() =
        context.getString(R.string.courier_order_dialog_positive_button_error)

    fun getArrive(arrive: String): String {
        return context.getString(R.string.courier_orders_confirm_arrive, arrive)
    }

    fun getBoxCountAndVolume(boxCount: Int, volume: Int): String {
        val v = context.resources.getQuantityString(R.plurals.volume, abs(volume), volume)
        return context.getString(R.string.courier_orders_count, boxCount, v)
    }

}