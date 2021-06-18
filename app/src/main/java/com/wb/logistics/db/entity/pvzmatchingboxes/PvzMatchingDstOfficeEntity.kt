package com.wb.logistics.db.entity.pvzmatchingboxes

import androidx.room.ColumnInfo

data class PvzMatchingDstOfficeEntity(
    @ColumnInfo(name = "pvz_match_dst_office_id")
    val id: Int,
    @ColumnInfo(name = "pvz_match_dst_office_name")
    val name: String,
    @ColumnInfo(name = "pvz_match_dst_office_full_address")
    val fullAddress: String,
    @ColumnInfo(name = "pvz_match_dst_office_longitude")
    val longitude: Double,
    @ColumnInfo(name = "pvz_match_dst_office_latitude")
    val latitude: Double,
)