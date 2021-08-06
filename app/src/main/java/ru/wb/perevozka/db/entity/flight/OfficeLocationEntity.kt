package ru.wb.perevozka.db.entity.flight

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class OfficeLocationEntity(
    @ColumnInfo(name = "office_location_id")
    val id: Int,
)
