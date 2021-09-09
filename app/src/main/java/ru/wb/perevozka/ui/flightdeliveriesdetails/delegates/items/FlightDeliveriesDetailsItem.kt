package ru.wb.perevozka.ui.flightdeliveriesdetails.delegates.items

import ru.wb.perevozka.mvvm.model.base.BaseItem

data class FlightDeliveriesDetailsItem(
    val barcode: String,
    val data: String,
    override var idView: Int,
) : BaseItem