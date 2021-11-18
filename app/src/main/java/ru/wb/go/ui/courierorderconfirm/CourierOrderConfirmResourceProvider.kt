package ru.wb.go.ui.courierorderconfirm

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider

class CourierOrderConfirmResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getVersionApp(version: String) = context.getString(R.string.app_version, version)

    fun getOrder(id: Int): String {
        return context.getString(R.string.courier_orders_confirm_order, id)
    }

    fun getCoast(amount: String): String {
        return context.getString(R.string.courier_orders_confirm_coast, amount)
    }

    fun getCarNumber(carNumber: String): String {
        return context.getString(R.string.courier_orders_confirm_car_number, carNumber)
    }

    fun getArrive(arrive: String): String {
        return context.getString(R.string.courier_orders_confirm_arrive, arrive)
    }

    fun getPvz(count: Int): String {
        return context.getString(R.string.courier_orders_confirm_pvz, count)
    }

    fun getVolume(minBoxesCount: Int, volume: Int): String {
//        val v = context.resources.getQuantityString(R.plurals.volume, abs(volume), volume)
        return context.getString(R.string.courier_orders_confirm_volume, minBoxesCount, volume)
    }

}