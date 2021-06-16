package com.wb.logistics.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wb.logistics.app.EXPORT_SCHEMA_DATABASE
import com.wb.logistics.app.VERSION_DATABASE
import com.wb.logistics.db.dao.*
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadedBoxEntity
import com.wb.logistics.db.entity.dcunloadedboxes.DcUnloadedReturnBoxEntity
import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxEntity

@Database(
    entities = [FlightEntity::class,
        FlightOfficeEntity::class,
        MatchingBoxEntity::class,
        FlightBoxEntity::class,
        AttachedBoxEntity::class,
        UnloadedBoxEntity::class,
        ReturnBoxEntity::class,
        DcUnloadedBoxEntity::class,
        DcUnloadedReturnBoxEntity::class],
    version = VERSION_DATABASE,
    exportSchema = EXPORT_SCHEMA_DATABASE
)

abstract class AppDatabase : RoomDatabase() {
    abstract val flightDao: FlightDao
    abstract val attachedBoxDao: AttachedBoxDao
    abstract val unloadingBoxDao: UnloadingBoxDao
    abstract val returnBoxDao: ReturnBoxDao
    abstract val dcUnloadingBoxDao: DcUnloadingBoxDao
    abstract val flightMatchingDao: FlightBoxDao
}