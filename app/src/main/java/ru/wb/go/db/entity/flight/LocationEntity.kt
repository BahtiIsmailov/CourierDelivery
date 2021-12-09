package ru.wb.go.db.entity.flight

import androidx.room.Embedded
import androidx.room.Entity

@Entity
data class LocationEntity(
    @Embedded val office: OfficeLocationEntity,
    val getFromGPS: Boolean,
)

