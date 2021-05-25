package com.wb.logistics.db.entity.dcunloadedboxes

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DcUnloadedBoxEntity(

    @ColumnInfo(name = "flight_box_id")
    val flightId: Int,

    val isManualInput: Boolean,

    @PrimaryKey
    val barcode: String,
    val updatedAt: String,
    val attachAt: String,
    @Embedded
    val currentOffice: DcUnloadedCurrentOfficeEntity,

    )