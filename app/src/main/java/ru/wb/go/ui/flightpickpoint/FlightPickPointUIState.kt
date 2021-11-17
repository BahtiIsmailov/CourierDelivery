package ru.wb.go.ui.flightpickpoint

sealed class FlightPickPointUIState {
    data class Error(val message: String) : FlightPickPointUIState()
}