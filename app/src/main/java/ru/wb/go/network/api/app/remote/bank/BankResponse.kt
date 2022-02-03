package ru.wb.go.network.api.app.remote.bank

data class BankResponse(
    val id: Int,
    val bic: String,
    val name: String,
    val correspondentAccount: String,
    val isDeleted: Boolean,
)