package ru.wb.go.ui.courierordertimer

sealed class CourierOrderTimerProgressState {

    object Progress : CourierOrderTimerProgressState()

    object ProgressComplete : CourierOrderTimerProgressState()

}