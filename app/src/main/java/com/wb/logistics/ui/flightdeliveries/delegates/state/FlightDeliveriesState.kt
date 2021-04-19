package com.wb.logistics.ui.flightdeliveries.delegates.state

interface FlightDeliveriesState {
    fun handler(action: FlightDeliveriesActionCallback)
}