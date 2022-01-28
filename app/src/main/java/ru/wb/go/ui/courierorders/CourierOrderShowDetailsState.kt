package ru.wb.go.ui.courierorders

sealed class CourierOrderShowDetailsState {

    object Disable : CourierOrderShowDetailsState()

    object Enable : CourierOrderShowDetailsState()

    object Progress : CourierOrderShowDetailsState()

}