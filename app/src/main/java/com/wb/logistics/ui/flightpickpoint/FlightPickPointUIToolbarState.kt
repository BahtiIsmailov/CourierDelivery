package com.wb.logistics.ui.flightpickpoint

sealed class FlightPickPointUIToolbarState {

    data class Flight(val label: String) : FlightPickPointUIToolbarState()
    data class Delivery(val label: String) : FlightPickPointUIToolbarState()

}
