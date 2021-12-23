package ru.wb.go.network.api.app.entity.accounts

data class AccountsEntity(
    val inn: String,
    val data: List<AccountEntity>
)