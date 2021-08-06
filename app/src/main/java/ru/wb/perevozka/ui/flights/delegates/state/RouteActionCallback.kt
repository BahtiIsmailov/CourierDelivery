package ru.wb.perevozka.ui.flights.delegates.state

interface RouteActionCallback {
    fun onChangedRoute(idData: Int, idView: Int)
}