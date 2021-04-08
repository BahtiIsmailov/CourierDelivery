package com.wb.logistics.db.entity.boxinfo

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class BoxInfoDstOfficeEntity(
    @ColumnInfo(name = "box_info_dst_office_id")
    val id: Int,
    @ColumnInfo(name = "box_info_dst_office_name")
    val name: String,
    @ColumnInfo(name = "box_info_dst_office_full_address")
    val fullAddress: String,
    @ColumnInfo(name = "box_info_dst_office_full_longitude")
    val longitude: Double,
    @ColumnInfo(name = "box_info_dst_office_full_latitude")
    val latitude: Double,
)