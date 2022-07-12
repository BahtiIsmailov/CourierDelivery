package ru.wb.go.db.entity.courierlocal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CourierOrderLocalEntity(
    @PrimaryKey
    @ColumnInfo(name = "order_id")
    val id: Int,
    val rowId: Int,
    val routeID: Int,
    val route: String?,
    val gate: String,
    val minPrice: Int,
    val minVolume: Int,
    val minBoxesCount: Int,
    val reservedDuration: String,
    val reservedAt: String,
    val taskDistance: String
)
