package com.wb.logistics.db.entity.unloadedboxes

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class UnloadedDstOfficeEntity(
    @ColumnInfo(name = "dst_office_id")
    val id: Int,
)