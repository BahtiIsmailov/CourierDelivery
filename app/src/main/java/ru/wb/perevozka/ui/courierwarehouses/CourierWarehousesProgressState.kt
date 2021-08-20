package ru.wb.perevozka.ui.courierwarehouses

sealed class CourierWarehousesProgressState {

    object Progress : CourierWarehousesProgressState()

    object ProgressComplete : CourierWarehousesProgressState()

}