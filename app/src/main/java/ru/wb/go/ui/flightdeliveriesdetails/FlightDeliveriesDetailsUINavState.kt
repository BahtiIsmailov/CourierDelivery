package ru.wb.go.ui.flightdeliveriesdetails

sealed class FlightDeliveriesDetailsUINavState {
    data class NavigateToUpload(val currentOfficeId: Int) : FlightDeliveriesDetailsUINavState()
}
