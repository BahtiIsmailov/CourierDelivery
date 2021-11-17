package ru.wb.go.ui.flights

sealed class FlightsUINavState {

    object NavigateToReceptionBox : FlightsUINavState()
    object NavigateToNetworkInfoDialog : FlightsUINavState()
    object NavigateToReturnBalanceDialog : FlightsUINavState()

}
