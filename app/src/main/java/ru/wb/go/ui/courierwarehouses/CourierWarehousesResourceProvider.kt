package ru.wb.go.ui.courierwarehouses

import android.content.Context
import ru.wb.go.R
import ru.wb.go.mvvm.BaseServicesResourceProvider

class CourierWarehousesResourceProvider(private val context: Context) :
    BaseServicesResourceProvider(context) {

    fun getEmptyList() =
        context.getString(R.string.courier_warehouse_empty_list)

    fun getDialogEmptyTitle() =
        context.getString(R.string.courier_warehouse_dialog_empty_title)
    fun getDialogEmptyMessage() =
        context.getString(R.string.courier_warehouse_dialog_empty_message)
    fun getDialogEmptyButton() =
        context.getString(R.string.courier_warehouse_dialog_empty_button)

    fun getDistance(distance:String) =
        context.getString(R.string.warehouse_distance, distance)
    fun getWarehouseMapIcon() = R.drawable.ic_courier_map_warehouse

    fun getWarehouseMapSelectedIcon() = R.drawable.ic_courier_map_warehouse_select

}