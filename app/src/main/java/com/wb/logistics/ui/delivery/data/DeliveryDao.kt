package com.wb.logistics.ui.delivery.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface DeliveryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(deliveryEntities: List<DeliveryEntity>)

    @Query("SELECT * FROM delivery")
    fun findAll(): LiveData<List<DeliveryEntity>>
}