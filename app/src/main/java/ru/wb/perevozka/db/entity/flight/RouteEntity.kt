package ru.wb.perevozka.db.entity.flight

import androidx.room.ColumnInfo
import androidx.room.Entity

@Entity
data class RouteEntity(

    @ColumnInfo(name = "route_id")
    val id: Int,
    val changed: Boolean,
    @ColumnInfo(name = "route_name")
    val name: String,
)