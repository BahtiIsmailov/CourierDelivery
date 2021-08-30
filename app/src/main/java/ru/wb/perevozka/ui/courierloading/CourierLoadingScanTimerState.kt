package ru.wb.perevozka.ui.courierloading

sealed class CourierLoadingScanTimerState {

    data class Timer(val timeAnalog: Float, val timeDigit: String) : CourierLoadingScanTimerState()

    object TimeIsOut : CourierLoadingScanTimerState()

}