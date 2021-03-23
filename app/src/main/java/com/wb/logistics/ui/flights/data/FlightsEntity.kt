package com.wb.logistics.ui.flights.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "delivery")
data class FlightsEntity(
    @PrimaryKey val
    id: Long,
)
