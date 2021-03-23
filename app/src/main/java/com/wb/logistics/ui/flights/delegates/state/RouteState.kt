package com.wb.logistics.ui.flights.delegates.state

interface RouteState {
    fun handler(action: RouteActionCallback)
}