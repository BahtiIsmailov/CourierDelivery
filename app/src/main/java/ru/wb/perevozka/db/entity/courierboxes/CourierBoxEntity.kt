package ru.wb.perevozka.db.entity.courierboxes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CourierBoxEntity(

    @PrimaryKey
    val qrcode: String,
    val address: String,
    val dstOffice: String,
    val whenLoaded: String,
    val whenUnloaded: String,

    )