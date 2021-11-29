package ru.wb.go.db.entity.flight

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class DcEntity(

    @ColumnInfo(name = "dc_id")
    val id: Int,
    @ColumnInfo(name = "dc_name")
    val name: String,
    @ColumnInfo(name = "dc_fullAddress")
    val fullAddress: String,
    val longitude: Double,
    val latitude: Double,
)

