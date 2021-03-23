package com.wb.logistics.ui.flights.data

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface FlightsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(receptionEntities: List<FlightsEntity>)

    @Query("SELECT * FROM Reception")
    fun findAll(): LiveData<List<FlightsEntity>>
}