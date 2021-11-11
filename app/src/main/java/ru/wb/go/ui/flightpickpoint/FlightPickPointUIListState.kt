package ru.wb.go.ui.flightpickpoint

import ru.wb.go.mvvm.model.base.BaseItem

sealed class FlightPickPointUIListState {

    data class ShowFlight(val items: List<BaseItem>, val receptionBox: String) : FlightPickPointUIListState()
    data class ProgressFlight(val items: List<BaseItem>, val receptionBox: String) : FlightPickPointUIListState()
    data class UpdateFlight(val items: List<BaseItem>, val receptionBox: String) : FlightPickPointUIListState()

}
