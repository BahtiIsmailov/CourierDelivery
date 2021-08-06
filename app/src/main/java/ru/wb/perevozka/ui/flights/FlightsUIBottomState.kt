package ru.wb.perevozka.ui.flights

sealed class FlightsUIBottomState {
    object ScanBox : FlightsUIBottomState()
    object ReturnBox : FlightsUIBottomState()
}
