package ru.wb.go.ui.courierorders

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider
import kotlin.math.abs

class CourierOrdersResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getOrder(order: Int): String {
        return context.getString(R.string.courier_orders_order, order)
    }

    fun getCoast(coast: String): String {
        return context.getString(R.string.courier_orders_coast, coast)
    }

    fun getBoxCountBox(boxCount: Int): String {
        return context.getString(R.string.courier_orders_count_box, boxCount)
    }

    fun getVolume(volume: Int): String {
        val v = context.resources.getQuantityString(R.plurals.volume, abs(volume), volume)
        return context.getString(R.string.courier_orders_volume, v)
    }

    fun getCountPvz(pvzCount: Int): String {
        val address = context.resources.getQuantityString(R.plurals.address, abs(pvzCount), pvzCount)
        return context.getString(R.string.courier_orders_count_pvz, address)
    }

    fun getArrive(arrive: String): String {
        return context.getString(R.string.courier_orders_arrive, arrive)
    }

    fun getDialogEmpty() = context.getString(R.string.courier_orders_confirm_dialog_empty)

    fun getWarehouseMapSelectedIcon() = R.drawable.ic_courier_map_warehouse_select
    fun getOrderMapIcon() = R.drawable.ic_courier_map_order
    fun getOrderMapSelectedIcon() = R.drawable.ic_courier_map_order_selected

}