package com.wb.logistics.ui.flightdeliveriesdetails

sealed class FlightDeliveriesDetailsUINavState {
    data class NavigateToUpload(val currentOfficeId: Int) : FlightDeliveriesDetailsUINavState()
}
