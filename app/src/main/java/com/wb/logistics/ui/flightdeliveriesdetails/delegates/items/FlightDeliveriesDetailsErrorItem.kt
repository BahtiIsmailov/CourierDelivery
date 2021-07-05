package com.wb.logistics.ui.flightdeliveriesdetails.delegates.items

import com.wb.logistics.mvvm.model.base.BaseItem

data class FlightDeliveriesDetailsErrorItem(
    val barcode: String,
    val data: String,
    override var idView: Int,
) : BaseItem