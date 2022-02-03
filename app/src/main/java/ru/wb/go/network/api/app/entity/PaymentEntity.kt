package ru.wb.go.network.api.app.entity

data class PaymentEntity(
        val recipientBankName: String,
        val recipientBankBik: String,
        val recipientCorrespondentAccount: String,
        val recipientName: String,
        val recipientAccount: String = "",
        val recipientInn: String,
)