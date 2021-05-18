package com.wb.logistics.ui.flightdeliveriesdetails

import com.wb.logistics.mvvm.model.base.BaseItem

sealed class FlightDeliveriesDetailsItemsState {

    data class Items(val items: List<BaseItem>) : FlightDeliveriesDetailsItemsState()

}