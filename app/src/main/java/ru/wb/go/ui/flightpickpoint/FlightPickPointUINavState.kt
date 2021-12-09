package ru.wb.go.ui.flightpickpoint

sealed class FlightPickPointUINavState {
    object ShowDeliveryDialog : FlightPickPointUINavState()
    object NavigateToDelivery : FlightPickPointUINavState()
}
