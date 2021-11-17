package ru.wb.go.ui.flightdeliveries.delegates.items

import ru.wb.go.mvvm.model.base.BaseItem

data class FlightDeliveriesItem(
    val address: String,
    val deliverCount: String,
    val returnedCount: String,
    val isEnabled: Boolean,
    override var idView: Int,
) : BaseItem
