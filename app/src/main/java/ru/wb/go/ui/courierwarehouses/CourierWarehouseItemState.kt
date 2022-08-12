package ru.wb.go.ui.courierwarehouses

sealed class CourierWarehouseItemState {

    data class InitItems(val items: MutableSet<CourierWarehouseItem>) :
        CourierWarehouseItemState()

    data class UpdateItems(val items: MutableList<CourierWarehouseItem>) :
        CourierWarehouseItemState()

    data class Empty(val info: String) : CourierWarehouseItemState()

    object NoInternet : CourierWarehouseItemState()

    data class UpdateItem(val position: Int, val item: CourierWarehouseItem) :
        CourierWarehouseItemState()

    data class ScrollTo(val position: Int) :
        CourierWarehouseItemState()

    object Success : CourierWarehouseItemState()

}