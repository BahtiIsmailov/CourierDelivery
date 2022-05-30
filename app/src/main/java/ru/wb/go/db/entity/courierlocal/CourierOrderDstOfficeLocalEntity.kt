package ru.wb.go.db.entity.courierlocal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CourierOrderDstOfficeLocalEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "dst_office_key")
    val key: Int,
    @ColumnInfo(name = "dst_office_id")
    val id: Int,
    @ColumnInfo(name = "dst_office_order_id")
    val orderId: Int,
    @ColumnInfo(name = "dst_office_name")
    val name: String,
    @ColumnInfo(name = "dst_office_full_address")
    val fullAddress: String,
    @ColumnInfo(name = "dst_office_longitude")
    val longitude: Double,
    @ColumnInfo(name = "dst_office_latitude")
    val latitude: Double,
    @Deprecated("use CourierOrderVisitedOfficeLocalEntity")
    @ColumnInfo(name = "dst_office_visited_at")
    val visitedAt: String,
    @ColumnInfo(name = "dst_office_work_times")
    val workTimes: String,
    @ColumnInfo(name = "dst_office_is_unusual_time")
    val isUnusualTime: Boolean
)