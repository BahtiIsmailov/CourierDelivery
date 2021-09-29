package ru.wb.perevozka.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierTimerEntity

@Dao
interface CourierWarehouseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(courierWarehouseEntity: CourierWarehouseLocalEntity): Completable

    @Query("SELECT * FROM CourierWarehouseLocalEntity")
    fun read(): Single<CourierWarehouseLocalEntity>

    @Query("DELETE FROM CourierWarehouseLocalEntity")
    fun deleteAll()

    @Query("SELECT Warehouse.name AS name, CourierOrder.order_id AS orderId, CourierOrder.minPrice AS price, CourierOrder.minBoxesCount AS boxesCount, CourierOrder.minVolume AS volume, COALESCE(CourierOrder.gate, '-') AS gate, CourierOrderDstOffice.countPvz AS countPvz, CourierOrder.reservedDuration AS reservedDuration, CourierOrder.reservedAt AS reservedAt FROM (SELECT name FROM CourierWarehouseLocalEntity) AS Warehouse, (SELECT * FROM CourierOrderLocalEntity) AS CourierOrder, (SELECT COUNT(*) as countPvz FROM CourierOrderDstOfficeLocalEntity) AS CourierOrderDstOffice")
    fun courierTimerEntity(): Single<CourierTimerEntity>

}