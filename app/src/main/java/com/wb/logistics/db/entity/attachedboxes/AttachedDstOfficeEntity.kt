package com.wb.logistics.db.entity.attachedboxes

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class AttachedDstOfficeEntity(
    @ColumnInfo(name = "dst_office_id")
    val id: Int,
)