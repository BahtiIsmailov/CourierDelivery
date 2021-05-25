package com.wb.logistics.db.entity.dcunloadedboxes

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class DcUnloadedCurrentOfficeEntity(
    @ColumnInfo(name = "current_office_id")
    val id: Int,
)