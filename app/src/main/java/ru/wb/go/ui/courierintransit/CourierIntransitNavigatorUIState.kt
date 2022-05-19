package ru.wb.go.ui.courierintransit

sealed class CourierIntransitNavigatorUIState {

    object Enable: CourierIntransitNavigatorUIState()
    object Disable: CourierIntransitNavigatorUIState()

}