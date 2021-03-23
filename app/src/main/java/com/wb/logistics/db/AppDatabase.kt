package com.wb.logistics.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wb.logistics.ui.flights.data.FlightsDao
import com.wb.logistics.ui.flights.data.FlightsEntity
import com.wb.logistics.ui.reception.data.ReceptionDao
import com.wb.logistics.ui.reception.data.ReceptionEntity

@Database(
    entities = [FlightsEntity::class, ReceptionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val deliveryDao: FlightsDao
    abstract val receptionDao: ReceptionDao
}