package com.wb.logistics.ui.flightpickpoint

sealed class FlightPickPointUINavState {
    object ShowDeliveryDialog : FlightPickPointUINavState()
    object NavigateToDelivery : FlightPickPointUINavState()
}
