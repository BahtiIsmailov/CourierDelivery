package ru.wb.go.network.api.app.entity

import ru.wb.go.network.api.app.StatusOK

data class BillingTransactionEntity(
        val statusDescription: String,
        val status: Int,
        val statusOK: StatusOK,
        val uuid: String,
        val value: Int,
        val createdAt: String,
)