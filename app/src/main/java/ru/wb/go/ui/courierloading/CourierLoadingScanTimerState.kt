package ru.wb.go.ui.courierloading

sealed class CourierLoadingScanTimerState {

    data class Timer(val timeAnalog: Float, val timeDigit: String) :
        CourierLoadingScanTimerState()

    data class Info(val gate: String) : CourierLoadingScanTimerState()

    data class TimeIsOut(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierLoadingScanTimerState()

    object Stopped : CourierLoadingScanTimerState()

}