package com.wb.logistics.db.entity.flight

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class CarEntity(

    @ColumnInfo(name = "car_id")
    val id: Int,
    val plateNumber: String,
)

