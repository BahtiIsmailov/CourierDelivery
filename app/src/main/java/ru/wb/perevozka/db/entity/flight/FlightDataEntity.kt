package ru.wb.perevozka.db.entity.flight

import androidx.room.Embedded
import androidx.room.Relation

data class FlightDataEntity(

    @Embedded val flightEntity: FlightEntity,
    @Relation(
        parentColumn = "flight_id",
        entityColumn = "office_flight_id"
    )
    val officeEntity: List<FlightOfficeEntity>,

    )
