package com.wb.logistics.ui.flightdeliveries

sealed class FlightDeliveriesUINavState {

    object ShowDeliveryDialog : FlightDeliveriesUINavState()
    object NavigateToDelivery : FlightDeliveriesUINavState()
    data class NavigateToUpload(val dstOfficeId: Int, val officeName: String) : FlightDeliveriesUINavState()

    object Empty : FlightDeliveriesUINavState()
}
