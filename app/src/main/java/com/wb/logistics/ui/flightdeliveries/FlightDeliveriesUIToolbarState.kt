package com.wb.logistics.ui.flightdeliveries

sealed class FlightDeliveriesUIToolbarState {

    data class Flight(val label: String) : FlightDeliveriesUIToolbarState()
    data class Delivery(val label: String) : FlightDeliveriesUIToolbarState()

}
