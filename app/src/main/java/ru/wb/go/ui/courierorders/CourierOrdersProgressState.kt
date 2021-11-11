package ru.wb.go.ui.courierorders

sealed class CourierOrdersProgressState {
    object Progress : CourierOrdersProgressState()
    object Complete : CourierOrdersProgressState()
}
