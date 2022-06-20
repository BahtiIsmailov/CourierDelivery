package ru.wb.go.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity

@Dao
interface CourierWarehouseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(courierWarehouseEntity: CourierWarehouseLocalEntity)

    @Query("SELECT * FROM CourierWarehouseLocalEntity")
    suspend fun read():  CourierWarehouseLocalEntity

    @Query("DELETE FROM CourierWarehouseLocalEntity")
    suspend fun deleteAll()
}