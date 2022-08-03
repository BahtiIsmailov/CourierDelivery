package ru.wb.go.ui.courierorders

import android.content.Context
import androidx.annotation.DrawableRes
import ru.wb.go.R
import ru.wb.go.app.DEFAULT_CAR_TYPE
import ru.wb.go.mvvm.BaseServicesResourceProvider

class CourierOrdersResourceProvider(private val context: Context) :
    BaseServicesResourceProvider(context) {

    fun getOrder(order: Int): String {
        return order.toString()
    }

    fun getCost(coast: String): String {
        return context.getString(R.string.orderCost, coast)
    }

    fun getCargo(volume: Int): String {
        return context.getString(R.string.orderCargo, volume)//, boxCount
    }

    fun getCargo(volume: Int, boxCount: Int): String {
        return context.getString(R.string.orderCardCargo, volume, boxCount)
    }
    fun getCountPvz(pvzCount: Int): String {
        val address = context.getString(R.string.itemOfficeCount, pvzCount)
        return context.getString(R.string.courier_orders_count_pvz, address)
    }

    fun getArrive(arrive: Int): String {
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

    fun getOfficeMapIcon() = R.drawable.ic_address_point_normal

    fun getOfficeMapSelectedIcon() = R.drawable.ic_address_point_select

    fun getOfficeMapTimeIcon() = R.drawable.ic_address_point_time_normal

    fun getOfficeMapSelectedTimeIcon() = R.drawable.ic_address_point_time_select

    fun getConfirmTitleDialog(orderNumber: Int) =
        context.getString(R.string.courier_orders_details_dialog_title, orderNumber)

    fun getConfirmMessageDialog(carNumber: String, volume: String, reserve: String,boxCountWithRouteId:Int) =
        context.getString(
            R.string.courier_orders_details_dialog_message,
            carNumber,
            volume,
            reserve,
            boxCountWithRouteId
        )

    fun getConfirmPositiveDialog() =
        context.getString(R.string.courier_orders_details_dialog_positive_button)

    fun getConfirmNegativeDialog() =
        context.getString(R.string.courier_orders_details_dialog_negative_button)

    fun getTaskReject() =
        context.getString(R.string.courier_orders_details_dialog_task_reject)


    @DrawableRes
    fun getTypeIcons(type: Int): Int {
        return if (type == DEFAULT_CAR_TYPE) R.drawable.ic_car_type_uncknown
        else context.resources.obtainTypedArray(R.array.car_type_icon).getResourceId(type, 0)
    }

    fun getWorkTimeEmpty() =
        context.getString(R.string.courier_orders_details_work_time_empty)
}