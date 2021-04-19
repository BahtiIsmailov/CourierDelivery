package com.wb.logistics.db.entity.scannedboxes

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class SrcOfficeEntity(
    @ColumnInfo(name = "src_office_id")
    val id: Int,
)