package ru.wb.go.ui.flightdeliveries

sealed class FlightDeliveriesUINavState {
    data class NavigateToUpload(val dstOfficeId: Int) : FlightDeliveriesUINavState()
    data class NavigateToDialogComplete(val description: String) : FlightDeliveriesUINavState()
    object NavigateToCongratulation : FlightDeliveriesUINavState()
    data class  NavigateToUnloadDetails(val dstOfficeId: Int, val officeName: String) : FlightDeliveriesUINavState()
}
