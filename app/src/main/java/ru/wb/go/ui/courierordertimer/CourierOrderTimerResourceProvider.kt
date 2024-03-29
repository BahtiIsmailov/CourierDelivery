package ru.wb.go.ui.courierordertimer

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseServicesResourceProvider
import kotlin.math.abs

class CourierOrderTimerResourceProvider(private val context: Context) :
    BaseServicesResourceProvider(context) {

    fun getOrder(id: Int): String {
        return context.getString(R.string.courier_orders_details_order, id)
    }

    fun getRoute(rout:String?) : String{
        return context.getString(R.string.courier_orders_details_route, rout)
    }

    fun getCoast(amount: String?): String {
        return context.getString(R.string.courier_orders_details_coast, amount)
    }

    fun getPvz(pvz: Int): String {
        return context.getString(R.string.courier_orders_details_pvz, pvz)
    }

    fun getBoxCountAndVolume(boxCount: Int, volume: Int): String {
        val v = context.resources.getQuantityString(R.plurals.volume, abs(volume), volume)
        return context.getString(R.string.courier_orders_count, boxCount, v)
    }

    fun getCargo(volume: Int, boxCount: Int): String {
        return context.getString(R.string.orderCardCargo, volume, boxCount)
    }
    fun getDialogTimerSkipTitle() = context.getString(R.string.courier_orders_timer_dialog_skip_title)
    fun getDialogTimerSkipMessage() = context.getString(R.string.courier_orders_timer_dialog_skip_message)
    fun getDialogTimerPositiveButton() = context.getString(R.string.courier_orders_timer_dialog_positive_button)
    fun getDialogTimerNegativeButton() = context.getString(R.string.courier_orders_timer_dialog_negative_button)

    fun getDialogTimerTitle() = context.getString(R.string.courier_orders_timer_dialog_title)
    fun getDialogTimerMessage() = context.getString(R.string.courier_orders_timer_dialog_message)
    fun getDialogTimerButton() = context.getString(R.string.courier_orders_timer_dialog_button)

}