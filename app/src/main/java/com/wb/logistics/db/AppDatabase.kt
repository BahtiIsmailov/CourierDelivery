package com.wb.logistics.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.wb.logistics.ui.delivery.data.DeliveryDao
import com.wb.logistics.ui.delivery.data.DeliveryEntity

@Database(entities = [DeliveryEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract val deliveryDao: DeliveryDao
}