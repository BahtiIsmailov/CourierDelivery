package ru.wb.perevozka.ui.courierorderconfirm

import android.content.Context
import ru.wb.perevozka.R
import ru.wb.perevozka.mvvm.BaseMessageResourceProvider

class CourierOrderConfirmResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getOrder(id: Int): String {
        return context.getString(R.string.courier_orders_confirm_order, id)
    }

    fun getCoast(amount: String): String {
        return context.getString(R.string.courier_orders_confirm_coast, amount)
    }

    fun getCarNumber(carNumber: String): String {
        return context.getString(R.string.courier_orders_confirm_car_number, carNumber)
    }

    fun getArrive(arrive: Int): String {
        return context.getString(R.string.courier_orders_confirm_arrive, arrive)
    }

    fun getVolume(volume: Int): String {
//        val v = context.resources.getQuantityString(R.plurals.volume, abs(volume), volume)
        return context.getString(R.string.courier_orders_confirm_volume, volume)
    }

}