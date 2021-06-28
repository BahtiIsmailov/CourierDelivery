package com.wb.logistics.ui.flightdeliveries

sealed class FlightDeliveriesUIProgressState {
    object CompletePositiveDeliveryProgress : FlightDeliveriesUIProgressState()
    object CompleteNegativeDeliveryProgress : FlightDeliveriesUIProgressState()
    object CompleteDeliveryNormal : FlightDeliveriesUIProgressState()
}
