package com.wb.logistics.ui.flights

sealed class FlightLoaderUINavState {
    object NavigateToFlight : FlightLoaderUINavState()
    object NavigateToReceptionScan : FlightLoaderUINavState()
    object NavigateToPickUpPoint : FlightLoaderUINavState()
    object NavigateToDelivery : FlightLoaderUINavState()
    data class NavigateToUnloading(val officeId: Int, val shortAddress: String) : FlightLoaderUINavState()
}