package ru.wb.perevozka.ui.flights

sealed class FlightsUINavState {

    object NavigateToReceptionBox : FlightsUINavState()
    object NavigateToNetworkInfoDialog : FlightsUINavState()
    object NavigateToReturnBalanceDialog : FlightsUINavState()

}
