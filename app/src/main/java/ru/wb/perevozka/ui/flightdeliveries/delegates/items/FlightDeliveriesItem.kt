package ru.wb.perevozka.ui.flightdeliveries.delegates.items

import ru.wb.perevozka.mvvm.model.base.BaseItem

data class FlightDeliveriesItem(
    val address: String,
    val deliverCount: String,
    val returnedCount: String,
    val isEnabled: Boolean,
    override var idView: Int,
) : BaseItem
