package ru.wb.perevozka.db.entity.courierlocal

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CourierOrderLocalEntity(
    @PrimaryKey
    @ColumnInfo(name = "order_id")
    val id: Int,
    val routeID: Int,
    val gate: String,
//    @Embedded
//    val srcOffice: CourierOrderSrcOfficeLocalEntity,
    val minPrice: Int,
    val minVolume: Int,
    val minBoxesCount: Int,
    val reservedDuration: String,
    val reservedAt: String,
)
