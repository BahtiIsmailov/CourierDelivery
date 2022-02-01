package ru.wb.go.ui.courierorders

sealed class CourierOrdersShowDetailsState {

    object Disable : CourierOrdersShowDetailsState()

    object Enable : CourierOrdersShowDetailsState()

    object Progress : CourierOrdersShowDetailsState()

}