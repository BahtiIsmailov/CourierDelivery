package ru.wb.go.ui.courierwarehouses

sealed class CourierWarehousesProgressState {

    object Progress : CourierWarehousesProgressState()

    object ProgressComplete : CourierWarehousesProgressState()

}