package ru.wb.perevozka.ui.courierbilling.delegates.items

import ru.wb.perevozka.mvvm.model.base.BaseItem

data class CourierBillingNegativeItem(
    val date: String,
    val amount: String,
    override var idView: Int,
) : BaseItem
