package ru.wb.perevozka.network.api.app.remote.billing

data class BillingTransactionResponse(
    val uuid: String,
    val value: Int,
    val createdAt: String,
)