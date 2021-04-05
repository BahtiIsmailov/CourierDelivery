package com.wb.logistics.db.entity.flight

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CarEntity(
    @PrimaryKey
    @ColumnInfo(name = "car_id")
    val id: Int,
    val plateNumber: String,
)

