package com.wb.logistics.ui.flightpickpoint.delegates.items

import com.wb.logistics.mvvm.model.base.BaseItem

data class FlightPickPointItem(
    val address: String,
    val deliverCount: String,
    val pickupCount: String,
    val isPickupPoint : Boolean,
    override var idView: Int,
) : BaseItem
