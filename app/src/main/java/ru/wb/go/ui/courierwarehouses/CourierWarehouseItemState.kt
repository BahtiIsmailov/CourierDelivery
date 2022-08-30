package ru.wb.go.ui.courierwarehouses

sealed class CourierWarehouseItemState {

    object NoInternet : CourierWarehouseItemState()

    object Success : CourierWarehouseItemState()

}