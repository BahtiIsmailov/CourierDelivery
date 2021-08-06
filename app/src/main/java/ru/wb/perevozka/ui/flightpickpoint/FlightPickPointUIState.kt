package ru.wb.perevozka.ui.flightpickpoint

sealed class FlightPickPointUIState {
    data class Error(val message: String) : FlightPickPointUIState()
}