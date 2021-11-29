package ru.wb.go.db.entity.courierlocal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CourierOrderVisitedOfficeLocalEntity(
    @PrimaryKey
    @ColumnInfo(name = "visited_office_dst_office_id")
    val dstOfficeId: Int,
    @ColumnInfo(name = "visited_office_visited_at")
    val visitedAt: String,
    @ColumnInfo(name = "visited_office_is_unload")
    val isUnload: Boolean,
)