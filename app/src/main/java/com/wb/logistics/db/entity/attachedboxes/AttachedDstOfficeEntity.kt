package com.wb.logistics.db.entity.attachedboxes

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class AttachedDstOfficeEntity(
    @ColumnInfo(name = "dst_office_id")
    val id: Int,
    @ColumnInfo(name = "dst_office_name")
    val name: String,
    @ColumnInfo(name = "dst_office_full_address")
    val fullAddress: String,
    @ColumnInfo(name = "dst_office_longitude")
    val longitude: Double,
    @ColumnInfo(name = "dst_office_latitude")
    val latitude: Double,
)