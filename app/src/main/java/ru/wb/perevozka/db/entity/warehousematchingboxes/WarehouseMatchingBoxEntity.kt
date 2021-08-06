package ru.wb.perevozka.db.entity.warehousematchingboxes

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class WarehouseMatchingBoxEntity(

    @PrimaryKey
    val barcode: String,

    @Embedded
    val srcOffice: WarehouseMatchingSrcOfficeEntity,
    @Embedded
    val dstOffice: WarehouseMatchingDstOfficeEntity,
)