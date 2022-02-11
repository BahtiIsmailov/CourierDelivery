package ru.wb.go.ui.courierwarehouses

sealed class CourierWarehousesShowOrdersState {

    object Disable : CourierWarehousesShowOrdersState()

    object Enable : CourierWarehousesShowOrdersState()

}