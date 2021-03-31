package com.wb.logistics.ui.flights

sealed class FlightsUINavState {

    object NavigateToReceptionBox : FlightsUINavState()
    object NavigateToNetworkInfoDialog : FlightsUINavState()
    object NavigateToReturnBalanceDialog : FlightsUINavState()

    object Empty : FlightsUINavState()
}
