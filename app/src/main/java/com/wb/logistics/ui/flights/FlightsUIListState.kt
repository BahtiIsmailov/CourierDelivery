package com.wb.logistics.ui.flights

import com.wb.logistics.mvvm.model.base.BaseItem

sealed class FlightsUIListState {

    data class ShowFlight(val items: List<BaseItem>, val countFlight: String) : FlightsUIListState()
    data class ProgressFlight(val items: List<BaseItem>, val countFlight: String) : FlightsUIListState()
    data class UpdateFlight(val items: List<BaseItem>, val countFlight: String) : FlightsUIListState()

}
