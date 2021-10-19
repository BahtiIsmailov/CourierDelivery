package ru.wb.perevozka.ui.courierorders.delegates.items

import ru.wb.perevozka.mvvm.model.base.BaseItem

data class CourierOrderItem(
    val order: String,
    val arrive: String,
    val volume: String,
    val pvzCount: String,
    val coast: String,
    override var idView: Int,
) : BaseItem
