package com.wb.logistics.db.entity.returnboxes

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class ReturnDstOfficeEntity(
    @ColumnInfo(name = "dst_office_id")
    val id: Int,
)