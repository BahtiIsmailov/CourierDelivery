package ru.wb.go.ui.courierorderdetails

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider
import kotlin.math.abs

class CourierOrderDetailsResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getOrder(id: Int): String {
        return context.getString(R.string.courier_orders_details_order, id)
    }

    fun getCoast(amount: String): String {
        return context.getString(R.string.courier_orders_details_coast, amount)
    }

    fun getArrive(arrive: String): String {
        return context.getString(R.string.courier_orders_confirm_arrive, arrive)
    }

    fun getPvz(pvz: Int): String {
        return context.getString(R.string.courier_orders_details_pvz, pvz)
    }


    fun getBoxCountAndVolume(boxCount: Int, volume: Int): String {
        val v = context.resources.getQuantityString(R.plurals.volume, abs(volume), volume)
        return context.getString(R.string.courier_orders_count, boxCount, v)
    }

    fun getOfficeMapIcon() = R.drawable.ic_unload_office_map_empty

    fun getOfficeMapSelectedIcon() = R.drawable.ic_unload_office_map_empty_select

}