package ru.wb.go.db.entity.flight

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class FlightEntity(
    @PrimaryKey
    @ColumnInfo(name = "flight_id")
    val id: Int,
    val gate: Int,
    @Embedded
    val dc: DcEntity,
    @Embedded
    val driver: DriverEntity,
    @Embedded
    val route: RouteEntity?,
    @Embedded
    val car: CarEntity,
    val plannedDate: String,
    val startedDate: String,
    val status: String,
    @Embedded val location: LocationEntity,
)

