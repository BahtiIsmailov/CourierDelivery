package ru.wb.perevozka.db.entity.deliveryerrorbox

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DeliveryUnloadingErrorBoxEntity(

    @PrimaryKey
    val barcode: String,
    val dstOfficeId: Int,
    val updatedAt: String,
    val fullAddress: String,
    val onBoard: Boolean,

    val errorOfficeId: Int?,
    val errorOfficeFullAddress: String?,

)