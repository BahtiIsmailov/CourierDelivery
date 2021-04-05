package com.wb.logistics.db.entity.boxinfo

import androidx.room.Entity

@Entity
data class FlightEntity(
    val id: Int,
    val gate: Int,
    val plannedDate: String,
    val isAttached: Boolean,
)