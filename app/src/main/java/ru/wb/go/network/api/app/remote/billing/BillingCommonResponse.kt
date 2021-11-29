package ru.wb.go.network.api.app.remote.billing

data class BillingCommonResponse(
    val id: String,
    val balance: Int,
    val entity: BillingResponse,
    val transactions: List<BillingTransactionResponse>
)