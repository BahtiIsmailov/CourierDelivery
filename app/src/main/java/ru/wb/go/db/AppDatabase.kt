package ru.wb.go.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.wb.go.app.EXPORT_SCHEMA_DATABASE
import ru.wb.go.app.VERSION_DATABASE
import ru.wb.go.db.dao.*
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.db.entity.courierboxes.CourierBoxEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderVisitedOfficeLocalEntity
import ru.wb.go.db.entity.deliveryerrorbox.DeliveryErrorBoxEntity
import ru.wb.go.db.entity.flighboxes.FlightBoxEntity
import ru.wb.go.db.entity.flight.FlightEntity
import ru.wb.go.db.entity.flight.FlightOfficeEntity
import ru.wb.go.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
import ru.wb.go.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity

@Database(
    entities = [
        CourierWarehouseLocalEntity::class,
        CourierOrderLocalEntity::class,
        CourierOrderDstOfficeLocalEntity::class,
        CourierOrderVisitedOfficeLocalEntity::class,
        CourierBoxEntity::class,
        FlightEntity::class,
        FlightOfficeEntity::class,
        WarehouseMatchingBoxEntity::class,
        PvzMatchingBoxEntity::class,
        FlightBoxEntity::class,
        DeliveryErrorBoxEntity::class],
    version = VERSION_DATABASE,
    exportSchema = EXPORT_SCHEMA_DATABASE
)

abstract class AppDatabase : RoomDatabase() {
    abstract val courierWarehouseDao: CourierWarehouseDao
    abstract val courierOrderDao: CourierOrderDao
    abstract val courierBoxDao: CourierBoxDao
    abstract val flightDao: FlightDao
    abstract val flightMatchingDao: FlightBoxDao
    abstract val warehouseMatchingBoxDao: WarehouseMatchingBoxDao
    abstract val pvzMatchingBoxDao: PvzMatchingBoxDao
    abstract val deliveryErrorBoxDao: DeliveryErrorBoxDao
}