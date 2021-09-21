package ru.wb.perevozka.ui.courierintransit

sealed class CourierIntransitTimeState {

    data class Time(val time: String) : CourierIntransitTimeState()

}