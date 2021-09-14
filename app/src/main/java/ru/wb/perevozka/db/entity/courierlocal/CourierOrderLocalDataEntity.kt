package ru.wb.perevozka.db.entity.courierlocal

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation
import ru.wb.perevozka.db.entity.flight.FlightEntity

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
