package ru.wb.go.ui.courierorderdetails

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider

class CourierOrderDetailsResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getCarNumber(carNumber: String): String {
        return context.getString(R.string.courier_orders_confirm_car_number, carNumber)
    }

    fun getCarNumberEmpty(): String {
        return context.getString(R.string.courier_orders_confirm_car_number_empty)
    }

    fun getOrder(order: Int): String {
        return order.toString()
    }

    fun getCost(cost: String): String {
        return context.getString(R.string.orderCost, cost)
    }

    fun getCargo(boxCount: Int, volume: Int): String {
        return context.getString(R.string.orderCargo, boxCount, volume)
    }

    fun getCountPvz(pvzCount: Int): String {
        val address =
            context.getString(R.string.itemOfficeCount, pvzCount)
        return context.getString(R.string.courier_orders_count_pvz, address)
    }

    fun getArrive(arrive: String): String {
        return context.getString(R.string.courier_orders_arrive, arrive)
    }

    fun getWarehouseMapSelectedIcon() = R.drawable.ic_courier_map_warehouse_select

    fun getOfficeMapIcon() = R.drawable.ic_order_details_office

    fun getOfficeMapSelectedIcon() = R.drawable.ic_intransit_office_map_empty_select

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

}