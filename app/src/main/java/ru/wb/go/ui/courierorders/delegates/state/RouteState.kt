package ru.wb.go.ui.courierorders.delegates.state

interface RouteState {
    fun handler(action: RouteActionCallback)
}