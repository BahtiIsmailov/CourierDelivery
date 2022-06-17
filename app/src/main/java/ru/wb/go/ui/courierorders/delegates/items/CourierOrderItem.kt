package ru.wb.go.ui.courierorders.delegates.items

import ru.wb.go.mvvm.model.base.BaseItem

data class CourierOrderItem(
    val lineNumber: String,
    val orderId: String,
    val cost: String,
    val cargo: String,
    val countPvz: String,
    val arrive: String,
    var isSelected: Boolean,
    var taskDistance : String,
    override var idView: Int,
) : BaseItem
