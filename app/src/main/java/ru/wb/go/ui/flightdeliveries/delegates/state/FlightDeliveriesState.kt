package ru.wb.go.ui.flightdeliveries.delegates.state

interface FlightDeliveriesState {
    fun handler(action: FlightDeliveriesActionCallback)
}