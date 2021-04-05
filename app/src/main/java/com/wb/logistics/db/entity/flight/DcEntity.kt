package com.wb.logistics.db.entity.flight

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DcEntity(
    @PrimaryKey
    @ColumnInfo(name = "dc_id")
    val id: Int,
    @ColumnInfo(name = "dc_name")
    val name: String,
    @ColumnInfo(name = "dc_fullAddress")
    val fullAddress: String,
    val longitude: Double,
    val latitude: Double,
)

