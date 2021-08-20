package ru.wb.perevozka.ui.courierorders

sealed class CourierOrderProgressState {
    object Progress : CourierOrderProgressState()
    object Complete : CourierOrderProgressState()
}
