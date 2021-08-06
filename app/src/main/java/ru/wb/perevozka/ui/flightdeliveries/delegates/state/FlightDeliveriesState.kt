package ru.wb.perevozka.ui.flightdeliveries.delegates.state

interface FlightDeliveriesState {
    fun handler(action: FlightDeliveriesActionCallback)
}