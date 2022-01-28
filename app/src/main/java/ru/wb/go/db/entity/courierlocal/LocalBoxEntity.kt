package ru.wb.go.db.entity.courierlocal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "boxes")
data class LocalBoxEntity(
    @PrimaryKey
    @ColumnInfo(name = "box_id")
    val boxId: String,
    val address: String,
    @ColumnInfo(name = "office_id")
    val officeId: Int,
    @ColumnInfo(name = "loading_at")
    val loadingAt: String,
    @ColumnInfo(name = "delivered_at")
    val deliveredAt: String,
)
