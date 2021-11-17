package ru.wb.go.ui.flightdeliveries.delegates.items

import ru.wb.go.mvvm.model.base.BaseItem

data class FlightDeliveriesUnloadItem(
    val address: String,
    val unloadedCount: String,
    val returnCount: String,
    override var idView: Int,
) : BaseItem
