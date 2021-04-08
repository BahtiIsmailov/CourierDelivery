package com.wb.logistics.db.entity.boxinfo

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class BoxInfoFlightEntity(
    @ColumnInfo(name = "box_info_flight_id")
    val id: Int,
    val gate: Int,
    val plannedDate: String,
    val isAttached: Boolean,
)