package ru.wb.go.ui.courierbilling.delegates.items

import ru.wb.go.mvvm.model.base.BaseItem

data class CourierBillingPositiveItem(
    val date: String,
    val time: String,
    val amount: String,
    override var idView: Int,
) : BaseItem
