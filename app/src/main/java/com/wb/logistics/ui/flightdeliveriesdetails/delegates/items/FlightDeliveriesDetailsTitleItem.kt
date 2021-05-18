package com.wb.logistics.ui.flightdeliveriesdetails.delegates.items

import com.wb.logistics.mvvm.model.base.BaseItem

data class FlightDeliveriesDetailsTitleItem(
    val title: String,
    val count: String,
    val isHeader: Boolean,
    override var idView: Int,
) : BaseItem