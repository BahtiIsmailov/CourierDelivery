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
    fun insert(courierWarehouseEntity: CourierWarehouseLocalEntity): Completable

    @Query("SELECT * FROM CourierWarehouseLocalEntity")
    fun read(): Single<CourierWarehouseLocalEntity>

    @Query("DELETE FROM CourierWarehouseLocalEntity")
    fun deleteAll()
}