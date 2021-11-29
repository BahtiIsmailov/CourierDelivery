package ru.wb.go.ui.courierorders.delegates.items

import ru.wb.go.mvvm.model.base.BaseItem

data class CourierOrderItem(
    val order: String,
    val arrive: String,
    val volume: String,
    val pvzCount: String,
    val coast: String,
    override var idView: Int,
) : BaseItem
