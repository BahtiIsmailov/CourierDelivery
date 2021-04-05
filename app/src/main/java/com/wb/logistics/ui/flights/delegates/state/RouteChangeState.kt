package com.wb.logistics.ui.flights.delegates.state;

data class RouteChangeState(val idData: Int, val idView: Int) :
    RouteState {
    override fun handler(action: RouteActionCallback) {
        action.onChangedRoute(idData, idView)
    }
}