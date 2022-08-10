package ru.wb.go.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity

@Dao
interface CourierWarehouseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(courierWarehouseEntity: CourierWarehouseLocalEntity)

    @Query("SELECT * FROM CourierWarehouseLocalEntity")
    suspend fun read():  CourierWarehouseLocalEntity

    @Query("SELECT * FROM CourierWarehouseLocalEntity WHERE warehouse_id=:id")
    suspend fun loadWarehousesFromId(id:Int):List<CourierWarehouseLocalEntity>

    @Query("DELETE FROM CourierWarehouseLocalEntity")
    suspend fun deleteAll()
}