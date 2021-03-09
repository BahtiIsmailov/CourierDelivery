package com.wb.logistics.ui.reception.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface ReceptionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(receptionEntities: List<ReceptionEntity>)

    @Query("SELECT * FROM Reception")
    fun findAll(): LiveData<List<ReceptionEntity>>
}