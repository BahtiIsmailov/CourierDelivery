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
    val ridMask:Long?,
    val gate: String,
    val minCost: String,
    val minVolume: Int,
    val minBoxesCount: Int,
    val reservedDuration: Int,
    val reservedAt: String,
    val taskDistance: String
)
