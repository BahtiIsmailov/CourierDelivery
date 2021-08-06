package ru.wb.perevozka.ui.flightdeliveries

import ru.wb.perevozka.mvvm.model.base.BaseItem

sealed class FlightDeliveriesUIListState {

    data class ShowFlight(
        val items: List<BaseItem>,
        val bottomState: FlightDeliveriesUIBottomState,
    ) : FlightDeliveriesUIListState()

    data class ProgressFlight(
        val items: List<BaseItem>,
        val bottomState: FlightDeliveriesUIBottomState,
    ) : FlightDeliveriesUIListState()

    data class UpdateFlight(
        val items: List<BaseItem>,
        val bottomState: FlightDeliveriesUIBottomState,
    ) : FlightDeliveriesUIListState()

}
