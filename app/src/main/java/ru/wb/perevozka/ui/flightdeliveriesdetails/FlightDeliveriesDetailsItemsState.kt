package ru.wb.perevozka.ui.flightdeliveriesdetails

import ru.wb.perevozka.mvvm.model.base.BaseItem

sealed class FlightDeliveriesDetailsItemsState {

    data class Items(val items: List<BaseItem>) : FlightDeliveriesDetailsItemsState()

}