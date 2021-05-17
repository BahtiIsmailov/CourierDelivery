package com.wb.logistics.ui.flightdeliveries.delegates.items

import com.wb.logistics.mvvm.model.base.BaseItem

data class FlightDeliveriesNotUnloadItem(
    val address: String,
    val unloadedCount: String,
    val returnCount: String,
    override var idView: Int,
) : BaseItem
