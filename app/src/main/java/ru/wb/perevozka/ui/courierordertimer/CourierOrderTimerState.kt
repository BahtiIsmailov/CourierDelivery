package ru.wb.perevozka.ui.courierordertimer

sealed class CourierOrderTimerState {

    data class Timer(val timeAnalog: Float, val timeDigit: String) : CourierOrderTimerState()

}