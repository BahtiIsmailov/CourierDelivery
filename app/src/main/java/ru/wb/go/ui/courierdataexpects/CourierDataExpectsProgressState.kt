package ru.wb.go.ui.courierdataexpects


sealed class CourierDataExpectsProgressState {

    object Complete : CourierDataExpectsProgressState()
    object ProgressData : CourierDataExpectsProgressState()

}