package ru.wb.go.ui.flightdeliveries.delegates.state

interface FlightDeliveriesActionCallback {
    fun onChangedRoute(idData: Int, idView: Int)
}