package ru.wb.go.db.entity.flight

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class DriverEntity(

    @ColumnInfo(name = "driver_id")
    val id: Int,
    @ColumnInfo(name = "driver_name")
    val name: String,
    @ColumnInfo(name = "driver_fullAddress")
    val fullAddress: String,
)
