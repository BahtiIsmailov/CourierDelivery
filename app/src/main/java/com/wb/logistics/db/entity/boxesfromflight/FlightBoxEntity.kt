package com.wb.logistics.db.entity.boxesfromflight

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FlightBoxEntity(

    @ColumnInfo(name = "box_flight_id")
    val flightId: Int,

    @PrimaryKey
    val barcode: String,
    @Embedded
    val srcOffice: SrcOfficeEntity,
    @Embedded
    val dstOffice: DstOfficeEntity,
    val smID: Int,
)