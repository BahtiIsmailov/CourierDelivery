package com.wb.logistics.ui.flightdeliveries

sealed class FlightDeliveriesUIBottomState {
    object GoToDelivery : FlightDeliveriesUIBottomState()
    object Empty : FlightDeliveriesUIBottomState()
}
