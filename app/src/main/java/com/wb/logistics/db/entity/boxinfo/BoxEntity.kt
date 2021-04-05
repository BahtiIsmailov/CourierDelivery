package com.wb.logistics.db.entity.boxinfo

import androidx.room.Entity

@Entity
data class BoxEntity(
    val barcode: String,
    val srcOffice: SrcOfficeEntity,
    val dstOffice: DstOfficeEntity,
    val smID: Int,
)