package ru.wb.go.db.entity.courierlocal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "offices")
data class LocalOfficeEntity(
    @PrimaryKey
    @ColumnInfo(name = "office_id")
    val officeId: Int,
    @ColumnInfo(name = "office_name")
    val officeName: String,
    val address: String,
    val longitude: Double,
    val latitude: Double,
    @ColumnInfo(name = "count_boxes")
    val countBoxes:Int,
    @ColumnInfo(name = "delivered_boxes")
    val deliveredBoxes:Int,
    @ColumnInfo(name = "is_online")
    val isOnline:Boolean,
    @ColumnInfo(name = "is_visited")
    val isVisited:Boolean,
)
