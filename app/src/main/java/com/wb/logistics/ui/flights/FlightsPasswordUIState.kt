package com.wb.logistics.ui.flights

import com.wb.logistics.mvvm.model.base.BaseItem

sealed class FlightsPasswordUIState {

    object NavigateToReceptionBox : FlightsPasswordUIState()
    object NavigateToNetworkInfoDialog : FlightsPasswordUIState()
    object NavigateToReturnBalanceDialog : FlightsPasswordUIState()

    data class ShowFlight(val items: List<BaseItem>, val countFlight: String) : FlightsPasswordUIState()
    data class ProgressFlight(val items: List<BaseItem>, val countFlight: String) : FlightsPasswordUIState()
    data class UpdateFlight(val items: List<BaseItem>, val countFlight: String) : FlightsPasswordUIState()

    object Empty : FlightsPasswordUIState()
}
