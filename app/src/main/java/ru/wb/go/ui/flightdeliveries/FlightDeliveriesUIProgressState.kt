package ru.wb.go.ui.flightdeliveries

sealed class FlightDeliveriesUIProgressState {
    object CompletePositiveDeliveryProgress : FlightDeliveriesUIProgressState()
    object CompleteNegativeDeliveryProgress : FlightDeliveriesUIProgressState()
    object CompleteDeliveryNormal : FlightDeliveriesUIProgressState()
}
