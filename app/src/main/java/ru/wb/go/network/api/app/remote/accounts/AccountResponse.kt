package ru.wb.go.network.api.app.remote.accounts

data class AccountResponse(
    val bic: String,
    val name: String,
    val correspondentAccount: String,
)