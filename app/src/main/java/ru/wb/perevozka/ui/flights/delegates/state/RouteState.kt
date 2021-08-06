package ru.wb.perevozka.ui.flights.delegates.state

interface RouteState {
    fun handler(action: RouteActionCallback)
}