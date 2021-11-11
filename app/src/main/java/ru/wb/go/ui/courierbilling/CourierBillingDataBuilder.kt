package ru.wb.go.ui.courierbilling

import ru.wb.go.mvvm.model.base.BaseItem
import ru.wb.go.network.api.app.entity.BillingTransactionEntity

interface CourierBillingDataBuilder {
    fun buildOrderItem(index: Int, entity: BillingTransactionEntity): BaseItem
}