package ru.wb.perevozka.ui.flightdeliveriesdetails

sealed class FlightDeliveriesDetailsUINavState {
    data class NavigateToUpload(val currentOfficeId: Int) : FlightDeliveriesDetailsUINavState()
}
