package com.wb.logistics.db.entity.attachedboxes

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class AttachedSrcOfficeEntity(
    @ColumnInfo(name = "src_office_id")
    val id: Int,
)