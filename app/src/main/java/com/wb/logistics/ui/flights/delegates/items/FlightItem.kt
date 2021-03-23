package com.wb.logistics.ui.flights.delegates.items

import com.wb.logistics.mvvm.model.base.BaseItem

data class FlightItem(
    val flight: String,
    val parkingNumber: String,
    val date: String,
    val time: String,
    val routesTitle: String,
    val routes: List<String>
) : BaseItem {

    private var _idView: Int = 0

    override var idView: Int
        get() = _idView
        set(value) {
            _idView = value
        }

}
