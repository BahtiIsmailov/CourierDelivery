package com.wb.logistics.db.entity.returnboxes

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class ReturnCurrentOfficeEntity(
    @ColumnInfo(name = "current_office_id")
    val id: Int,
)