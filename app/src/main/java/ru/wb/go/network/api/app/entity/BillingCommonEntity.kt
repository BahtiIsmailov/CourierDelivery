package ru.wb.go.network.api.app.entity

data class BillingCommonEntity(
    val id: String,
    val balance: Int,
    val entity: BillingEntity,
    val transactions: List<BillingTransactionEntity>
)