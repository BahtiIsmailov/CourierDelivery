package ru.wb.go.db.entity.courierlocal

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

@Entity
data class CourierOrderLocalDataEntity(
    @Embedded
    val courierOrderLocalEntity: CourierOrderLocalEntity,

    @Relation(
        parentColumn = "order_id",
        entityColumn = "dst_office_order_id"
    )
    val dstOffices: List<CourierOrderDstOfficeLocalEntity>,
)
