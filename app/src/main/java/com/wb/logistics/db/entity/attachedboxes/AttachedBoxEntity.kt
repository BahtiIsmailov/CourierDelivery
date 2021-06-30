package com.wb.logistics.db.entity.attachedboxes

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AttachedBoxEntity(
    @PrimaryKey
    val barcode: String,
    @Embedded
    val srcOffice: AttachedSrcOfficeEntity,
    @Embedded
    val dstOffice: AttachedDstOfficeEntity,
    val isManualInput: Boolean,
    val updatedAt: String,
)