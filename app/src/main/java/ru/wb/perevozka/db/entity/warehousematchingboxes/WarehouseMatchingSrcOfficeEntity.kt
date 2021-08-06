package ru.wb.perevozka.db.entity.warehousematchingboxes

import androidx.room.ColumnInfo

data class WarehouseMatchingSrcOfficeEntity(
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