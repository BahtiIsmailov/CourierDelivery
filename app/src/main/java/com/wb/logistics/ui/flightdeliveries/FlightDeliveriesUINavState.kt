package com.wb.logistics.ui.flightdeliveries

sealed class FlightDeliveriesUINavState {

    data class NavigateToUpload(val dstOfficeId: Int, val officeName: String) :
        FlightDeliveriesUINavState()

    data class NavigateToDialogComplete(val description: String) : FlightDeliveriesUINavState()
    object NavigateToCongratulation : FlightDeliveriesUINavState()
    object NavigateToUnloadDetails : FlightDeliveriesUINavState()
}
