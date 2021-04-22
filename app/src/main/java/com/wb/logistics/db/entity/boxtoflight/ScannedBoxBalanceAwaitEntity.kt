package com.wb.logistics.db.entity.boxtoflight

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class ScannedBoxBalanceAwaitEntity(
    @PrimaryKey
    val barcode: String,
    val isManualInput: Boolean,
    @Embedded
    val dstOffice: CurrentOfficeEntity,
    val updatedAt: String
)