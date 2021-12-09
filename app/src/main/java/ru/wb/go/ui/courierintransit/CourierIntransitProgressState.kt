package ru.wb.go.ui.courierintransit

sealed class CourierIntransitProgressState {

    object Progress : CourierIntransitProgressState()

    object ProgressComplete : CourierIntransitProgressState()

}