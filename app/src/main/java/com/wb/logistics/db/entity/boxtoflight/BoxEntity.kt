package com.wb.logistics.db.entity.boxtoflight

import androidx.room.Entity

@Entity
data class BoxEntity(
    val barcode: String,
    val isManualInput: Boolean,
    val dstOffice: CurrentOfficeEntity,
)