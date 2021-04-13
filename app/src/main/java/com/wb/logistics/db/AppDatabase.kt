package com.wb.logistics.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wb.logistics.db.entity.boxinfo.BoxInfoEntity
import com.wb.logistics.db.entity.boxtoflight.FlightBoxBalanceAwaitEntity
import com.wb.logistics.db.entity.flight.FlightEntity
import com.wb.logistics.db.entity.flight.FlightOfficeEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxEntity
import com.wb.logistics.db.entity.flightboxes.FlightBoxScannedEntity
import com.wb.logistics.db.entity.matchingboxes.MatchingBoxEntity

@Database(
    entities = [FlightEntity::class,
        FlightOfficeEntity::class,
        MatchingBoxEntity::class,
        FlightBoxEntity::class,
        FlightBoxScannedEntity::class,
        BoxInfoEntity::class,
        FlightBoxBalanceAwaitEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val flightDao: FlightDao
    abstract val boxDao: BoxDao
}