package com.wb.logistics.ui.flightdeliveries.delegates.items

import com.wb.logistics.mvvm.model.base.BaseItem

data class FlightDeliveriesItem(
    val address: String,
    val redoCount: String,
    val undoCount: String,
    val isShowBoxes: Boolean,
    val isEnabled: Boolean,
    val boxes: List<String>,
    override var idView: Int,
) : BaseItem {

}
