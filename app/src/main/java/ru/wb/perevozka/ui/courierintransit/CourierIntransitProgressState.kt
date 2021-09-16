package ru.wb.perevozka.ui.courierintransit

sealed class CourierIntransitProgressState {

    object Progress : CourierIntransitProgressState()

    object ProgressComplete : CourierIntransitProgressState()

}