package ru.wb.perevozka.db.entity.flightstatuses

import androidx.room.Entity

@Entity
data class FlightStatusEntity(
    val status: String,
    val description: String
)