package ru.wb.go.network.api.app.entity.accounts

data class AccountEntity(
    val bic: String,
    val name: String,
    val correspondentAccount: String,
)