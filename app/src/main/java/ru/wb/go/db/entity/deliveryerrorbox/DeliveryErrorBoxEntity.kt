package ru.wb.go.db.entity.deliveryerrorbox

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DeliveryErrorBoxEntity(

    @PrimaryKey
    val barcode: String,
    val currentOfficeId: Int,

)