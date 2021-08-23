package ru.wb.perevozka.ui.auth.courierexpects


sealed class CourierExpectsProgressState {

    object Complete : CourierExpectsProgressState()
    object Progress : CourierExpectsProgressState()

}