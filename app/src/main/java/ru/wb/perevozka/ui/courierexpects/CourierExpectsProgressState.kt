package ru.wb.perevozka.ui.courierexpects


sealed class CourierExpectsProgressState {

    object Complete : CourierExpectsProgressState()
    object Progress : CourierExpectsProgressState()

}