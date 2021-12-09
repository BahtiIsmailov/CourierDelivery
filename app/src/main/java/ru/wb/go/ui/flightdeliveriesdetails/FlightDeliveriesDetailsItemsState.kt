package ru.wb.go.ui.flightdeliveriesdetails

import ru.wb.go.mvvm.model.base.BaseItem

sealed class FlightDeliveriesDetailsItemsState {

    data class Items(val items: List<BaseItem>) : FlightDeliveriesDetailsItemsState()

}