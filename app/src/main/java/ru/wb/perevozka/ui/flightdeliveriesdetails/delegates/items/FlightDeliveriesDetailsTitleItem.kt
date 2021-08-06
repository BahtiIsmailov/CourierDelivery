package ru.wb.perevozka.ui.flightdeliveriesdetails.delegates.items

import ru.wb.perevozka.mvvm.model.base.BaseItem

data class FlightDeliveriesDetailsTitleItem(
    val title: String,
    val count: String,
    val isHeader: Boolean,
    override var idView: Int,
) : BaseItem