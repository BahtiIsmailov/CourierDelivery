package ru.wb.go.ui.courierwarehouses

sealed class CourierWarehouseItemState {

    data class InitItems(val items: MutableList<CourierWarehouseItem>) :
        CourierWarehouseItemState()

    data class UpdateItems(val index: Int, val items: MutableList<CourierWarehouseItem>) :
        CourierWarehouseItemState()

    data class Empty(val info: String) : CourierWarehouseItemState()

}