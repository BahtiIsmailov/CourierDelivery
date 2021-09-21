package ru.wb.perevozka.ui.courierunloading

sealed class CourierUnloadingScanTimerState {

    data class Timer(val timeAnalog: Float, val timeDigit: String) : CourierUnloadingScanTimerState()

    data class TimeIsOut(
        val type: Int,
        val title: String,
        val message: String,
        val button: String
    ) : CourierUnloadingScanTimerState()

    object Stopped : CourierUnloadingScanTimerState()

}