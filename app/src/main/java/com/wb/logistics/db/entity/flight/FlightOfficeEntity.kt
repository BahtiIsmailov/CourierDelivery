package com.wb.logistics.db.entity.flight

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FlightOfficeEntity(
    @PrimaryKey
    @ColumnInfo(name = "office_id")
    val id: Int,
    @ColumnInfo(name = "office_flight_id")
    val flightId: Int,
    @ColumnInfo(name = "office_name")
    val name: String,
    val fullAddress: String,
    val longitude: Double,
    val latitude: Double,
    val isUnloading: Boolean,
    val notUnloadingCause: String,

)
