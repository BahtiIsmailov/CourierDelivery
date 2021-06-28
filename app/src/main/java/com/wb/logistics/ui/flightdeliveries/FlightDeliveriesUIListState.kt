package com.wb.logistics.ui.flightdeliveries

import com.wb.logistics.mvvm.model.base.BaseItem

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
