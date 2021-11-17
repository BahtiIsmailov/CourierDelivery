package ru.wb.go.db.entity.courierlocal

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CourierOrderSrcOfficeLocalEntity(
    @PrimaryKey
    @ColumnInfo(name = "src_office_id")
    val id: Int,
    @ColumnInfo(name = "src_office_name")
    val name: String,
    @ColumnInfo(name = "src_office_full_address")
    val fullAddress: String,
    @ColumnInfo(name = "src_office_longitude")
    val longitude: Double,
    @ColumnInfo(name = "src_office_latitude")
    val latitude: Double,
)