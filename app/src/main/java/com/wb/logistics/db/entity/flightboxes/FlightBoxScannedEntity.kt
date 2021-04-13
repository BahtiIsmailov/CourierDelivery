package com.wb.logistics.db.entity.flightboxes

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FlightBoxScannedEntity(

    @ColumnInfo(name = "flight_box_id")
    val flightId: Int,

    @PrimaryKey
    val barcode: String,
    val gate: Int,
    @Embedded
    val srcOffice: SrcOfficeEntity,
    @Embedded
    val dstOffice: DstOfficeEntity,
    val smID: Int,
    val isManualInput: Boolean,
    val dstFullAddress: String,
)