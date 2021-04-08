package com.wb.logistics.db.entity.boxinfo

import androidx.room.Embedded
import androidx.room.Entity

@Entity
data class BoxEntity(
    val barcode: String,
    @Embedded
    val srcOffice: BoxInfoSrcOfficeEntity,
    @Embedded
    val dstOffice: BoxInfoDstOfficeEntity,
    val smID: Int,
)