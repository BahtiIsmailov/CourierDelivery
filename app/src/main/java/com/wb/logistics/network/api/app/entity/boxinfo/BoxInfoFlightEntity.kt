package com.wb.logistics.network.api.app.entity.boxinfo

import androidx.room.Entity

data class BoxInfoFlightEntity(
    val id: Int,
    val gate: Int,
    val plannedDate: String,
    val isAttached: Boolean,
)