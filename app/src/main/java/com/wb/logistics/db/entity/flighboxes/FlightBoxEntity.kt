package com.wb.logistics.db.entity.flighboxes

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

@Entity
data class FlightBoxEntity(

    @PrimaryKey
    val barcode: String,
    val updatedAt: String,
    @TypeConverters(BoxStatusConverter::class)
    val status: Int,
    val onBoard: Boolean,

    @Embedded
    val srcOffice: FlightSrcOfficeEntity,
    @Embedded
    val dstOffice: FlightDstOfficeEntity,

    )