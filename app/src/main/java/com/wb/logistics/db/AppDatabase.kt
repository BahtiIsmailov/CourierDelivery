package com.wb.logistics.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wb.logistics.ui.delivery.data.DeliveryDao
import com.wb.logistics.ui.delivery.data.DeliveryEntity
import com.wb.logistics.ui.reception.data.ReceptionDao
import com.wb.logistics.ui.reception.data.ReceptionEntity

@Database(
    entities = [DeliveryEntity::class, ReceptionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract val deliveryDao: DeliveryDao
    abstract val receptionDao: ReceptionDao
}