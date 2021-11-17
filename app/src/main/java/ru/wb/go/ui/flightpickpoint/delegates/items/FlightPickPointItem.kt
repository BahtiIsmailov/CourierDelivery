package ru.wb.go.ui.flightpickpoint.delegates.items

import ru.wb.go.mvvm.model.base.BaseItem

data class FlightPickPointItem(
    val address: String,
    val deliverCount: String,
    val pickupCount: String,
    val isPickupPoint : Boolean,
    override var idView: Int,
) : BaseItem
