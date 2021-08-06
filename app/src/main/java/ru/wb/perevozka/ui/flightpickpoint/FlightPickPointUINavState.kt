package ru.wb.perevozka.ui.flightpickpoint

sealed class FlightPickPointUINavState {
    object ShowDeliveryDialog : FlightPickPointUINavState()
    object NavigateToDelivery : FlightPickPointUINavState()
}
