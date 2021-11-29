package ru.wb.go.ui.courierintransit

sealed class CourierIntransitTimeState {

    data class Time(val time: String) : CourierIntransitTimeState()

}