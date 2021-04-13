package com.wb.logistics.db.entity.matchingboxes

import androidx.room.ColumnInfo

data class MatchingDstOfficeEntity(
    @ColumnInfo(name = "match_dst_office_id")
    val id: Int,
    @ColumnInfo(name = "match_dst_office_name")
    val name: String,
    @ColumnInfo(name = "match_dst_office_full_address")
    val fullAddress: String,
    @ColumnInfo(name = "match_dst_office_longitude")
    val longitude: Double,
    @ColumnInfo(name = "match_dst_office_latitude")
    val latitude: Double,
)