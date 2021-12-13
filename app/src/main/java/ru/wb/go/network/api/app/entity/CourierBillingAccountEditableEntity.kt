package ru.wb.go.network.api.app.entity

data class CourierBillingAccountEditableEntity(
    val userName: String,
    val inn: String,
    val account: String,
    val bik: String,
    val bank: String,
)

