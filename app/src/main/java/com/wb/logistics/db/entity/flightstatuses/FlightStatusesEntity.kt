package com.wb.logistics.db.entity.flightstatuses

import androidx.room.Entity

@Entity
data class FlightStatusesEntity(
    val data: List<FlightStatusEntity>
)