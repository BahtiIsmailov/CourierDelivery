package ru.wb.go.ui.courierexpects


sealed class CourierExpectsProgressState {

    object Complete : CourierExpectsProgressState()
    object Progress : CourierExpectsProgressState()

}