package ru.wb.go.db.entity.courierboxes

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CourierBoxEntity(

    @PrimaryKey
    val id: String,
    val address: String,
    val dstOfficeId: Int,
    val loadingAt: String,
    val deliveredAt: String,

    )