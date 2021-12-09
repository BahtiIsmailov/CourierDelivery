package ru.wb.go.ui.flights

sealed class FlightsUIBottomState {
    object ScanBox : FlightsUIBottomState()
    object ReturnBox : FlightsUIBottomState()
}
