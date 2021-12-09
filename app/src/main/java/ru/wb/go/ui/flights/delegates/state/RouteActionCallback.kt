package ru.wb.go.ui.flights.delegates.state

interface RouteActionCallback {
    fun onChangedRoute(idData: Int, idView: Int)
}