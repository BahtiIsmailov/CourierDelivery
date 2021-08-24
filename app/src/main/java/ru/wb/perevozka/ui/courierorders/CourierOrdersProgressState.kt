package ru.wb.perevozka.ui.courierorders

sealed class CourierOrdersProgressState {
    object Progress : CourierOrdersProgressState()
    object Complete : CourierOrdersProgressState()
}
