package com.wb.logistics.db.entity.attachedboxesawait

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AttachedBoxBalanceAwaitEntity(
    @PrimaryKey
    val barcode: String,
    val isManualInput: Boolean,
    @Embedded
    val dstOffice: AttachedBoxCurrentOfficeEntity,
)