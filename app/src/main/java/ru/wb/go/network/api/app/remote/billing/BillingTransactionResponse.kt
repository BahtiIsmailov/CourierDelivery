package ru.wb.go.network.api.app.remote.billing

data class BillingTransactionResponse(
    val uuid: String,
    val value: Int,
    val createdAt: String,
)