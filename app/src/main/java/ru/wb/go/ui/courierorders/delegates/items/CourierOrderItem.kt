package ru.wb.go.ui.courierorders.delegates.items

import ru.wb.go.mvvm.model.base.BaseItem

data class CourierOrderItem(
    val orderId: String,
    val order: String,
    val cost: String,
    val countBox: String,
    val volume: String,
    val countPvz: String,
    val arrive: String,
    var isSelected: Boolean,
    override var idView: Int,
) : BaseItem
