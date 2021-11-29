package ru.wb.go.network.api.app.entity

data class BillingTransactionEntity(
    val uuid: String,
    val value: Int,
    val createdAt: String,
)