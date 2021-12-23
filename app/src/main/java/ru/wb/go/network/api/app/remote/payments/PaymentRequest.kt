package ru.wb.go.network.api.app.remote.payments

data class PaymentRequest(
    val amount: Int,
    val recipientBankName: String,
    val recipientName: String,
    val recipientBankBik: String,
    val recipientCorrespondentAccount: String,
    val recipientAccount: String,
    val recipientInn: String,
    val recipientKpp: String
)