package ru.wb.go.network.api.app.remote.accounts

data class AccountRequest(
    val bic: String,
    val name: String,
    val correspondentAccount: String,
)