package ru.wb.go.network.api.app.entity.accounts

data class BankAccountsEntity(
    val inn: String,
    val data: List<AccountEntity>
)