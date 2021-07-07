package com.wb.logistics.ui.flightdeliveries.delegates.items

import com.wb.logistics.mvvm.model.base.BaseItem

data class FlightDeliveriesItem(
    val address: String,
    val deliverCount: String,
    val returnedCount: String,
    val isEnabled: Boolean,
    override var idView: Int,
) : BaseItem
