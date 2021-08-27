package ru.wb.perevozka.ui.courierordertimer

sealed class CourierOrderTimerProgressState {

    object Progress : CourierOrderTimerProgressState()

    object ProgressComplete : CourierOrderTimerProgressState()

}