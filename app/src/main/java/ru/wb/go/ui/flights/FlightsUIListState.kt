package ru.wb.go.ui.flights

import ru.wb.go.mvvm.model.base.BaseItem

sealed class FlightsUIListState {

    data class ShowFlight(val items: List<BaseItem>) : FlightsUIListState()
    data class ProgressFlight(val items: List<BaseItem>) : FlightsUIListState()
    data class UpdateFlight(val items: List<BaseItem>) : FlightsUIListState()

}
