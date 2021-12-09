package ru.wb.go.ui.flightdeliveriesdetails.delegates.items

import ru.wb.go.mvvm.model.base.BaseItem

data class FlightDeliveriesDetailsErrorItem(
    val barcode: String,
    val data: String,
    override var idView: Int,
) : BaseItem