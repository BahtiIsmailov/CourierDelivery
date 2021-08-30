package ru.wb.perevozka.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.wb.perevozka.app.EXPORT_SCHEMA_DATABASE
import ru.wb.perevozka.app.VERSION_DATABASE
import ru.wb.perevozka.db.dao.*
import ru.wb.perevozka.db.entity.courier.CourierOrderDstOfficeEntity
import ru.wb.perevozka.db.entity.courier.CourierOrderEntity
import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.perevozka.db.entity.deliveryerrorbox.DeliveryErrorBoxEntity
import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import ru.wb.perevozka.db.entity.flight.FlightEntity
import ru.wb.perevozka.db.entity.flight.FlightOfficeEntity
import ru.wb.perevozka.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
import ru.wb.perevozka.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity

@Database(
    entities = [
        CourierOrderLocalEntity::class,
        CourierOrderDstOfficeLocalEntity::class,
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
    abstract val courierOrderDao: CourierOrderDao
    abstract val courierBoxDao: CourierBoxDao
    abstract val flightDao: FlightDao
    abstract val flightMatchingDao: FlightBoxDao
    abstract val warehouseMatchingBoxDao: WarehouseMatchingBoxDao
    abstract val pvzMatchingBoxDao: PvzMatchingBoxDao
    abstract val deliveryErrorBoxDao: DeliveryErrorBoxDao
}