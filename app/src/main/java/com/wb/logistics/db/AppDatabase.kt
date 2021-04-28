package com.wb.logistics.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wb.logistics.db.dao.AttachedBoxDao
import com.wb.logistics.db.dao.FlightDao
import com.wb.logistics.db.dao.ReturnBoxDao
import com.wb.logistics.db.dao.UnloadingBoxDao
import com.wb.logistics.db.entity.attachedboxes.AttachedBoxEntity
import com.wb.logistics.db.entity.attachedboxesawait.AttachedBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity
import com.wb.logistics.db.entity.returnboxes.ReturnBoxEntity
import com.wb.logistics.db.entity.unloadedboxes.UnloadedBoxEntity

@Database(
    entities = [FlightEntity::class,
        FlightOfficeEntity::class,
        MatchingBoxEntity::class,
        FlightBoxEntity::class,
        AttachedBoxEntity::class,
        AttachedBoxBalanceAwaitEntity::class,
        UnloadedBoxEntity::class,
        ReturnBoxEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val flightDao: FlightDao
    abstract val attachedBoxDao: AttachedBoxDao
    abstract val unloadingBoxDao: UnloadingBoxDao
    abstract val returnBoxDao: ReturnBoxDao
}