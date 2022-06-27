package ru.wb.go.ui.courierintransit

sealed class CourierIntransitNavigatorUIState {

    data class Enable(val scheduleOrder:String): CourierIntransitNavigatorUIState()
    object Disable: CourierIntransitNavigatorUIState()

}