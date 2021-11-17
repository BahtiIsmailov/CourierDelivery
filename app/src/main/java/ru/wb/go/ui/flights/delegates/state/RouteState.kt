package ru.wb.go.ui.flights.delegates.state

interface RouteState {
    fun handler(action: RouteActionCallback)
}