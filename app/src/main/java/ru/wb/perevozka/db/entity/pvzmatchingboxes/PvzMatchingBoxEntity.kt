package ru.wb.perevozka.db.entity.pvzmatchingboxes

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PvzMatchingBoxEntity(

    @PrimaryKey
    val barcode: String,

    @Embedded
    val srcOffice: PvzMatchingSrcOfficeEntity,
    @Embedded
    val dstOffice: PvzMatchingDstOfficeEntity,
)