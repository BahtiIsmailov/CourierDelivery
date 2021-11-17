package ru.wb.go.ui.flightdeliveriesdetails.delegates.items

import ru.wb.go.mvvm.model.base.BaseItem

data class FlightDeliveriesDetailsTitleItem(
    val title: String,
    val count: String,
    val isHeader: Boolean,
    override var idView: Int,
) : BaseItem