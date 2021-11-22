package ru.wb.go.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.wb.go.app.EXPORT_SCHEMA_DATABASE
import ru.wb.go.app.VERSION_DATABASE
import ru.wb.go.db.dao.CourierBoxDao
import ru.wb.go.db.dao.CourierOrderDao
import ru.wb.go.db.dao.CourierWarehouseDao
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.db.entity.courierboxes.CourierBoxEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderVisitedOfficeLocalEntity

@Database(
    entities = [
        CourierWarehouseLocalEntity::class,
        CourierOrderLocalEntity::class,
        CourierOrderDstOfficeLocalEntity::class,
        CourierOrderVisitedOfficeLocalEntity::class,
        CourierBoxEntity::class, ],
    version = VERSION_DATABASE,
    exportSchema = EXPORT_SCHEMA_DATABASE
)

abstract class AppDatabase : RoomDatabase() {
    abstract val courierWarehouseDao: CourierWarehouseDao
    abstract val courierOrderDao: CourierOrderDao
    abstract val courierBoxDao: CourierBoxDao
}