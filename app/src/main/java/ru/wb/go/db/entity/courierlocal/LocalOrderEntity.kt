package ru.wb.go.db.entity.courierlocal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courier_order")
data class LocalOrderEntity(
    @PrimaryKey
    @ColumnInfo(name = "order_id")
    val orderId: Int,
    @ColumnInfo(name = "route_id")
    val routeID: Int,
    val gate: String,
    @ColumnInfo(name = "min_price")
    val minPrice: Int,
    @ColumnInfo(name = "route")
    val route: String,
    @ColumnInfo(name = "min_volume")
    val minVolume: Int,
    @ColumnInfo(name = "min_boxes")
    val minBoxes: Int,
    @ColumnInfo(name = "count_offices")
    val countOffices:Int,
    @ColumnInfo(name = "wb_user_id")
    val wbUserID: Int,
    @ColumnInfo(name = "car_number")
    val carNumber: String,
    @ColumnInfo(name = "reserved_at")
    val reservedAt: String,
    @ColumnInfo(name = "started_at")
    val startedAt: String,
    @ColumnInfo(name = "reserve_duration")
    val reservedDuration: String,
    val status: String,
    val cost: Int,
    @ColumnInfo(name = "src_id")
    val srcId: Int,
    @ColumnInfo(name = "src_name")
    val srcName: String,
    @ColumnInfo(name = "src_address")
    val srcAddress: String,
    @ColumnInfo(name = "src_long")
    val srcLongitude: Double,
    @ColumnInfo(name = "src_lat")
    val srcLatitude: Double,

)