package ru.wb.go.db.entity.pvzmatchingboxes

import androidx.room.ColumnInfo

data class PvzMatchingDstOfficeEntity(
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