package ru.wb.perevozka.ui.courierorders

import android.content.Context
import ru.wb.perevozka.R
import ru.wb.perevozka.mvvm.BaseMessageResourceProvider
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