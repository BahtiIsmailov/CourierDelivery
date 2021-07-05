package com.wb.logistics.db.entity.deliveryerrorbox

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DeliveryErrorBoxEntity(

    @PrimaryKey
    val barcode: String,
    val currentOfficeId: Int,
    val updatedAt: String,
    val fullAddress: String,

    )