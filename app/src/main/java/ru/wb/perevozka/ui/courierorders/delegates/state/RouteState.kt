package ru.wb.perevozka.ui.courierorders.delegates.state

interface RouteState {
    fun handler(action: RouteActionCallback)
}