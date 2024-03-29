package ru.wb.go.ui.courierordertimer

sealed class CourierOrderTimerState {

    data class Timer(val timeAnalog: Float, val timeDigit: String) : CourierOrderTimerState()

    data class TimerIsOut(val timeAnalog: Float, val timeDigit: String) : CourierOrderTimerState()

}