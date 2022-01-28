package ru.wb.go.db.dao

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.go.db.entity.courierlocal.*

@Dao
interface CourierOrderDao {

    //==============================================================================================
    //order
    //==============================================================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrder(orderEntity: CourierOrderLocalEntity): Completable

    @Transaction
    @Query("SELECT * FROM CourierOrderLocalEntity")
    fun orderData(): CourierOrderLocalDataEntity?

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

    @Query("DELETE FROM CourierOrderDstOfficeLocalEntity")
    fun deleteAllOffices()

    // ====================================
    // True Local Order
    @Insert
    fun addOrder(localOrder: LocalOrderEntity)

    @Query(
        """
        INSERT INTO offices(office_id, office_name, address, latitude, longitude, delivered_boxes, count_boxes, is_visited, is_online)
        SELECT dst_office_id, dst_office_name, dst_office_full_address, dst_office_latitude, dst_office_longitude,0,0,0,1
        FROM CourierOrderDstOfficeLocalEntity
    """
    )
    fun addOfficesFromReserve()

    @Transaction
    fun addOrderFromReserve(order: LocalOrderEntity) {
        addOrder(order)
        addOfficesFromReserve()
    }

    @Query("DELETE FROM courier_order")
    fun deleteOrder()

    @Query("SELECT * FROM courier_order")
    fun getOrder(): LocalOrderEntity?

    @Insert
    fun addOffices(offices: List<LocalOfficeEntity>)

    @Query("DELETE FROM offices")
    fun deleteOffices()

    @Query("SELECT * FROM offices")
    fun getOffices(): List<LocalOfficeEntity>

    @Query("SELECT * FROM offices WHERE office_id=:officeId")
    fun getOfficeById(officeId: Int): Single<LocalOfficeEntity>

    @Query("SELECT * FROM offices")
    fun getOfficesFlowable(): Flowable<List<LocalOfficeEntity>>

    @Query("UPDATE courier_order SET status=:status, started_at=:startedAt")
    fun setOrderStart(status: String, startedAt: String)

    @Query("DELETE FROM offices WHERE count_boxes=0")
    fun deleteNotUsedOffices()

    @Query("UPDATE courier_order SET status=:status, cost=:cost")
    fun setCost(status: String, cost: Int)

    @Transaction
    fun setOrderAfterLoadStatus(status: String, cost: Int) {
        deleteNotUsedOffices()
        setCost(status, cost)
    }

    @Query("UPDATE offices SET is_visited = 1, is_online=0 WHERE office_id=:officeId")
    fun setVisitOffice(officeId: Int)

    @Query("UPDATE offices SET is_online=1 ")
    fun setOnlineOffice()
}