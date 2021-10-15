package ru.wb.perevozka.network.api.app.entity

data class BillingTransactionEntity(
    val uuid: String,
    val value: Int,
    val createdAt: String,
)