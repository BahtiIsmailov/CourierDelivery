package com.wb.logistics.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wb.logistics.app.EXPORT_SCHEMA_DATABASE
import com.wb.logistics.app.VERSION_DATABASE
import com.wb.logistics.db.dao.*
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import com.wb.logistics.db.entity.pvzmatchingboxes.PvzMatchingBoxEntity
import com.wb.logistics.db.entity.warehousematchingboxes.WarehouseMatchingBoxEntity

@Database(
    entities = [FlightEntity::class,
        FlightOfficeEntity::class,
        WarehouseMatchingBoxEntity::class,
        PvzMatchingBoxEntity::class,
        FlightBoxEntity::class,
        AttachedBoxEntity::class],
    version = VERSION_DATABASE,
    exportSchema = EXPORT_SCHEMA_DATABASE
)

abstract class AppDatabase : RoomDatabase() {
    abstract val flightDao: FlightDao
    abstract val attachedBoxDao: AttachedBoxDao
    abstract val flightMatchingDao: FlightBoxDao
    abstract val warehouseMatchingBoxDao: WarehouseMatchingBoxDao
    abstract val pvzMatchingBoxDao: PvzMatchingBoxDao
}