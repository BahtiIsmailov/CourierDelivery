package ru.wb.perevozka.db.dao

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderVisitedOfficeLocalEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalEntity

@Dao
interface CourierOrderDao {

    //==============================================================================================
    //order
    //==============================================================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrder(orderEntity: CourierOrderLocalEntity): Completable

    @Query("SELECT * FROM CourierOrderLocalEntity")
    fun readOrder(): Single<CourierOrderLocalEntity>

    @Transaction
    @Query("SELECT * FROM CourierOrderLocalEntity")
    fun orderData(): Single<CourierOrderLocalDataEntity>

    @Transaction
    @Query("SELECT * FROM CourierOrderLocalEntity")
    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

    @Query("DELETE FROM CourierOrderLocalEntity")
    fun deleteAllOrder()

    // TODO: 15.09.2021 вынести в отдельный DAO
    //==============================================================================================
    //order dst offices
    //==============================================================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrderOffices(courierOrderDstOfficeLocalEntity: List<CourierOrderDstOfficeLocalEntity>): Completable

    @Query("SELECT * FROM CourierOrderDstOfficeLocalEntity WHERE dst_office_id = :id")
    fun findOfficeById(id: Int): Single<CourierOrderDstOfficeLocalEntity>

    @Query("DELETE FROM CourierOrderDstOfficeLocalEntity")
    fun deleteAllOffices()

    @Query("UPDATE CourierOrderDstOfficeLocalEntity SET dst_office_visited_at = :visitedAt WHERE dst_office_id = :officeId")
    fun updateVisitedAtOffice(officeId: Int, visitedAt: String): Completable

    @Query("INSERT INTO CourierOrderVisitedOfficeLocalEntity SELECT dstOfficeId, MAX(deliveredAt) as visitedAt, COUNT(CASE WHEN deliveredAt != '' THEN 1 END) == COUNT(*) AS isUnload FROM CourierBoxEntity GROUP BY dstOfficeId")
    fun updateVisitedOfficeByBoxes(): Completable

    // TODO: 15.09.2021 вынести в отдельный DAO
    //==============================================================================================
    //order visited office
    //==============================================================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertVisitedOffice(courierOrderVisitedOfficeLocalEntity: CourierOrderVisitedOfficeLocalEntity): Completable

}