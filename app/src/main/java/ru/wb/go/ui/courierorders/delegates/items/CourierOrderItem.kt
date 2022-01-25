package ru.wb.go.ui.courierorders.delegates.items

import ru.wb.go.mvvm.model.base.BaseItem

data class CourierOrderItem(
    val orderNumber: String,
    val order: String,
    val coast: String,
    val countBox: String,
    val volume: String,
    val countPvz: String,
    val arrive: String,
    var isSelected: Boolean,
    override var idView: Int,
) : BaseItem
