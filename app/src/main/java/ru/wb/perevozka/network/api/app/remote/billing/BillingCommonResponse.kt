package ru.wb.perevozka.network.api.app.remote.billing

data class BillingCommonResponse(
    val id: String,
    val balance: Int,
    val entity: BillingResponse,
    val transactions: List<BillingTransactionResponse>
)