package ru.wb.perevozka.network.api.app.entity

data class PaymentEntity(
    val amount: Int,
    val recipientBankName: String,
    val recipientName: String,
    val recipientBankBik: String,
    val recipientCorrespondentAccount: String,
    val recipientAccount: String,
    val recipientInn: String,
    val recipientKpp: String
)