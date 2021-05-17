package com.wb.logistics.ui.flightpickpoint.delegates.items

import com.wb.logistics.mvvm.model.base.BaseItem

data class FlightPickPointItem(
    val address: String,
    val redoCount: String,
    val isShowBoxes: Boolean,
    val boxes: List<String>,
    override var idView: Int,
) : BaseItem
