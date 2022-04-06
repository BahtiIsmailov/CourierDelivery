package ru.wb.go.ui.courierorders

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseMessageResourceProvider

class CourierOrdersResourceProvider(private val context: Context) :
    BaseMessageResourceProvider(context) {

    fun getOrder(order: Int): String {
        return order.toString()
    }

    fun getCost(coast: String): String {
        return context.getString(R.string.orderCost, coast)
    }

    fun getCargo(boxCount: Int, volume: Int): String {
        return context.getString(R.string.orderCargo, volume, boxCount)
    }

    fun getCountPvz(pvzCount: Int): String {
        val address = context.getString(R.string.itemOfficeCount, pvzCount)
        return context.getString(R.string.courier_orders_count_pvz, address)
    }

    fun getArrive(arrive: String): String {
        return context.getString(R.string.courier_orders_arrive, arrive)
    }

    fun getDialogEmpty() = context.getString(R.string.courier_orders_confirm_dialog_empty)

    fun getWarehouseMapSelectedIcon() = R.drawable.ic_courier_map_warehouse_select
    fun getOrderMapNoneBorderIcon() = R.drawable.ic_courier_map_order_none_border
    fun getOrderMapIcon() = R.drawable.ic_courier_map_order
    fun getOrderMapSelectedIcon() = R.drawable.ic_courier_map_order_selected

    fun getCarNumber(carNumber: String): String {
        return context.getString(R.string.courier_orders_confirm_car_number, carNumber)
    }

    fun getCarNumberEmpty(): String {
        return context.getString(R.string.courier_orders_confirm_car_number_empty)
    }

    fun getOfficeMapIcon() = R.drawable.ic_order_details_office

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