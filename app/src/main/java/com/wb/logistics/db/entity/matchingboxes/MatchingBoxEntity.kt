package com.wb.logistics.db.entity.matchingboxes

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MatchingBoxEntity(

    @PrimaryKey
    val barcode: String,

    @Embedded
    val srcOffice: MatchingSrcOfficeEntity,
    @Embedded
    val dstOffice: MatchingDstOfficeEntity,
    val smID: Int,
)