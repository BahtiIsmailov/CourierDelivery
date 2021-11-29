package ru.wb.go.ui.courierorderdetails

sealed class CourierOrderDetailsMapPoint {

    data class NavigateToPoint(val lat: Double, val long: Double) :
        CourierOrderDetailsMapPoint()

}