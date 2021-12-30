package ru.wb.go.ui.courierbilling.delegates.items

import ru.wb.go.mvvm.model.base.BaseItem

data class CourierBillingNegativeItem(
    val date: String,
    val time: String,
    val amount: String,
    val statusDescription: String,
    val statusIcon: Int,
    override var idView: Int,
) : BaseItem
