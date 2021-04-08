package com.wb.logistics.db.entity.flightboxes

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class DstOfficeEntity(
    @ColumnInfo(name = "dst_office_id")
    val id: Int,
)