package ru.wb.go.db.entity.courier

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CourierWarehouseLocalEntity(
    @PrimaryKey
    @ColumnInfo(name = "warehouse_id")
    val id: Int,
    val name: String,
    val fullAddress: String,
    val longitude: Double,
    val latitude: Double,
)