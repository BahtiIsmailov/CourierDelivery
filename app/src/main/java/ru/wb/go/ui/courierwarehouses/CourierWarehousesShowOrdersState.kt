package ru.wb.go.ui.courierwarehouses

import org.osmdroid.util.Distance
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity

sealed class CourierWarehousesShowOrdersState {

    object Disable : CourierWarehousesShowOrdersState()

    data class Enable(
        val warehouseItem: List<CourierWarehouseLocalEntity>?,
        val distance: String
    ) : CourierWarehousesShowOrdersState()

}