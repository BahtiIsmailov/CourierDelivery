package com.wb.logistics.ui.flightpickpoint

sealed class FlightPickPointUIState {
    data class Error(val message: String) : FlightPickPointUIState()
}