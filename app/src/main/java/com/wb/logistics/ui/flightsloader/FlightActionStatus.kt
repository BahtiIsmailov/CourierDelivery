package com.wb.logistics.ui.flightsloader

sealed class FlightActionStatus {

    data class NotAssigned(val delivery: String) : FlightActionStatus()
    data class Loading(val deliveryId: String) : FlightActionStatus()
    data class InTransit(val deliveryId: String) : FlightActionStatus()

}