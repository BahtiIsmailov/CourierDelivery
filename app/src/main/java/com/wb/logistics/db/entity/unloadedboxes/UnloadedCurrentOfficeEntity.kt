package com.wb.logistics.db.entity.unloadedboxes

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class UnloadedCurrentOfficeEntity(
    @ColumnInfo(name = "current_office_id")
    val id: Int,
)