package ru.wb.perevozka.ui.courierdelivery

sealed class CourierDeliveryMapPoint {

    data class NavigateToPoint(val lat: Double, val long: Double) :
        CourierDeliveryMapPoint()

}