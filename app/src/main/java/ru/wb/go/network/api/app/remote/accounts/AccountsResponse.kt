package ru.wb.go.network.api.app.remote.accounts

data class AccountsResponse(
    val inn: String,
    val data: List<AccountResponse>
)