package ru.wb.go.network.api.app.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CourierBillingAccountEntity(
    val surName: String,
    val inn: String,
    @PrimaryKey
    val account: String,
    val bank: String,
    val corAccount: String = "",
    val bik: String,
    val innBank: String = "",
    val kpp: String = "",
)