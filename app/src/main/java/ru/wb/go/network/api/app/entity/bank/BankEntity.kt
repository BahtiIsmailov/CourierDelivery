package ru.wb.go.network.api.app.entity.bank

data class BankEntity(
//    val id: Int,
    val bic: String,
    val name: String,
    val correspondentAccount: String,
    val isDeleted: Boolean,
)