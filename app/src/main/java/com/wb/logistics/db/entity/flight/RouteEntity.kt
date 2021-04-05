package com.wb.logistics.db.entity.flight

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class RouteEntity(
    @PrimaryKey
    @ColumnInfo(name = "route_id")
    val id: Int,
    val changed: Boolean,
    @ColumnInfo(name = "route_name")
    val name: String,
)