package ru.wb.perevozka.network.api.app.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CourierBillingAccountEntity(
    val firstName: String,
    val surName: String,
    val middleName: String,
    val inn: String,
    @PrimaryKey
    val account: String,
    val bank: String,
    val bik: String,
    val kpp: String,
    val corAccount: String,
    val innBank: String,
)