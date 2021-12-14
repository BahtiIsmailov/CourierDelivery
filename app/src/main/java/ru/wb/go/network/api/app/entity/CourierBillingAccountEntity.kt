package ru.wb.go.network.api.app.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CourierBillingAccountEntity(
    val userName: String,
    val inn: String,
    @PrimaryKey
    val correspondentAccount: String,
    val bic: String,
    val bank: String,
)

