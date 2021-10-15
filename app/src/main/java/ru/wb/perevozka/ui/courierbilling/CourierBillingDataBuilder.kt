package ru.wb.perevozka.ui.courierbilling

import ru.wb.perevozka.mvvm.model.base.BaseItem
import ru.wb.perevozka.network.api.app.entity.BillingTransactionEntity

interface CourierBillingDataBuilder {
    fun buildOrderItem(index: Int, entity: BillingTransactionEntity): BaseItem
}