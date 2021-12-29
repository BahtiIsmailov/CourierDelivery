package ru.wb.go.network.api.app.remote.payments

data class PaymentRequest(
        val recipientBankName: String,
        val recipientBankBik: String,
        val recipientCorrespondentAccount: String,
        val recipientName: String,
        val recipientAccount: String?,
        val recipientInn: String
)