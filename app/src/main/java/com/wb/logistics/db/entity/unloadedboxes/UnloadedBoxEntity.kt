package com.wb.logistics.db.entity.unloadedboxes

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class UnloadedBoxEntity(

    @ColumnInfo(name = "flight_box_id")
    val flightId: Int,

    val isManualInput: Boolean,

    @PrimaryKey
    val barcode: String,
    val updatedAt: String,
    @Embedded
    val currentOffice: UnloadedCurrentOfficeEntity,

)