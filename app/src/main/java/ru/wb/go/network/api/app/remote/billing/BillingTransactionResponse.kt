package ru.wb.go.network.api.app.remote.billing

data class BillingTransactionResponse(
        val statusDescription: String?,
        val status: Int,
        val statusOK: Boolean?,
        val uuid: String,
        val value: Int,
        val createdAt: String,
)