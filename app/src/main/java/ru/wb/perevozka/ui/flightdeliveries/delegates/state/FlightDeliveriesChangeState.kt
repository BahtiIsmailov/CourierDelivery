package ru.wb.perevozka.ui.flightdeliveries.delegates.state

data class FlightDeliveriesChangeState(val idData: Int, val idView: Int) :
    FlightDeliveriesState {
    override fun handler(action: FlightDeliveriesActionCallback) {
        action.onChangedRoute(idData, idView)
    }
}