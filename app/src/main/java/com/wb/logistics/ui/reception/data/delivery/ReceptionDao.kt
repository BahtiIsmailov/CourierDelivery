package com.wb.logistics.ui.reception.data.delivery

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.wb.logistics.ui.reception.data.delivery.Reception

@Dao
interface ReceptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(reception: Reception)
}