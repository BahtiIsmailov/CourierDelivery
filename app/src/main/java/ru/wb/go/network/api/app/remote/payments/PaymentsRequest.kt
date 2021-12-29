package ru.wb.go.network.api.app.remote.payments

data class PaymentsRequest(
        val id: String,
        val amount: Int,
        val payment: PaymentRequest
)