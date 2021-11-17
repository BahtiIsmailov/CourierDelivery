package ru.wb.go.ui.flightdeliveries

sealed class FlightDeliveriesUIBottomState {
    object ShowCompletePositiveDelivery : FlightDeliveriesUIBottomState()
    object ShowCompleteNegativeDelivery : FlightDeliveriesUIBottomState()
    object Empty : FlightDeliveriesUIBottomState()
}
