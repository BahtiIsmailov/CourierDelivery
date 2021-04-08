package com.wb.logistics.db.entity.flightboxes

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FlightBoxEntity(

    @ColumnInfo(name = "flight_box_id")
    val flightId: Int,

    @PrimaryKey
    val barcode: String,
    @Embedded
    val srcOffice: SrcOfficeEntity,
    @Embedded
    val dstOffice: DstOfficeEntity,
    val smID: Int,
)