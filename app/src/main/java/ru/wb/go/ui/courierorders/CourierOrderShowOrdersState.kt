package ru.wb.go.ui.courierorders

sealed class CourierOrderShowOrdersState {

    object Disable : CourierOrderShowOrdersState()

    object Enable : CourierOrderShowOrdersState()

    object Invisible : CourierOrderShowOrdersState()

    object Visible : CourierOrderShowOrdersState()

}