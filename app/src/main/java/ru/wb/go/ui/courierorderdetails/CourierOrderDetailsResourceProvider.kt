package ru.wb.go.ui.courierorderdetails

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider
import kotlin.math.abs

class CourierOrderDetailsResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getCarNumber(carNumber: String): String {
        return context.getString(R.string.courier_orders_confirm_car_number, carNumber)
    }

    fun getCarNumberEmpty(): String {
        return context.getString(R.string.courier_orders_confirm_car_number_empty)
    }

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
        val address =
            context.resources.getQuantityString(R.plurals.address, abs(pvzCount), pvzCount)
        return context.getString(R.string.courier_orders_count_pvz, address)
    }

    fun getArrive(arrive: String): String {
        return context.getString(R.string.courier_orders_arrive, arrive)
    }

    fun getWarehouseMapSelectedIcon() = R.drawable.ic_courier_map_warehouse_select

    fun getOfficeMapIcon() = R.drawable.ic_warehouse_detail_address

    fun getOfficeMapSelectedIcon() = R.drawable.ic_unload_office_map_empty_select

    fun getConfirmTitleDialog(orderNumber: Int) =
        context.getString(R.string.courier_orders_details_dialog_title, orderNumber)

    fun getConfirmMessageDialog(carNumber: String, volume: Int, reserve: String) =
        context.getString(
            R.string.courier_orders_details_dialog_message,
            carNumber,
            volume,
            reserve
        )

    fun getConfirmPositiveDialog() =
        context.getString(R.string.courier_orders_details_dialog_positive_button)

    fun getConfirmNegativeDialog() =
        context.getString(R.string.courier_orders_details_dialog_negative_button)

    fun getOrderIsNotExistTitleDialog() =
        context.getString(R.string.courier_orders_details_dialog_is_not_exist_title)

    fun getOrderIsNotExistMessageDialog() =
        context.getString(R.string.courier_orders_details_dialog_is_not_exist_message)

    fun getOrderIsNotExistButtonDialog() =
        context.getString(R.string.courier_orders_details_dialog_is_not_exist_button)
}