package com.wb.logistics.ui.flightdeliveries

sealed class FlightDeliveriesUIBottomState {
    object ShowCompletePositiveDelivery : FlightDeliveriesUIBottomState()
    object ShowCompleteNegativeDelivery : FlightDeliveriesUIBottomState()
    object Empty : FlightDeliveriesUIBottomState()
}
