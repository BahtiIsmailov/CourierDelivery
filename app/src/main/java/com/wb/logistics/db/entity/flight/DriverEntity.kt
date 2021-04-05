package com.wb.logistics.db.entity.flight

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class DriverEntity(
    @PrimaryKey
    @ColumnInfo(name = "driver_id")
    val id: Int,
    @ColumnInfo(name = "driver_name")
    val name: String,
    @ColumnInfo(name = "driver_fullAddress")
    val fullAddress: String,
)
