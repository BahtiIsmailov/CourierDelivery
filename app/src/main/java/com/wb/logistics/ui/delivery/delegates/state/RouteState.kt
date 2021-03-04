package com.wb.logistics.ui.delivery.delegates.state

interface RouteState {
    fun handler(action: RouteActionCallback)
}