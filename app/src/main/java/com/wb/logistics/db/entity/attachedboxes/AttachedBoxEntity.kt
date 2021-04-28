package com.wb.logistics.db.entity.attachedboxes

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class AttachedBoxEntity(

    @ColumnInfo(name = "flight_box_id")
    val flightId: Int,

    @PrimaryKey
    val barcode: String,
    val gate: Int,
    @Embedded
    val srcOffice: AttachedSrcOfficeEntity,
    @Embedded
    val dstOffice: AttachedDstOfficeEntity,
    val smID: Int,
    val isManualInput: Boolean,
    val dstFullAddress: String,
    val updatedAt: String,
)