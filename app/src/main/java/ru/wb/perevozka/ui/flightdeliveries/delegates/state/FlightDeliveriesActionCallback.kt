package ru.wb.perevozka.ui.flightdeliveries.delegates.state

interface FlightDeliveriesActionCallback {
    fun onChangedRoute(idData: Int, idView: Int)
}