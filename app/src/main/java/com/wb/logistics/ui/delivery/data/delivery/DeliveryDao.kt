package com.wb.logistics.ui.delivery.data.delivery

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface DeliveryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(delivery: Delivery)
}