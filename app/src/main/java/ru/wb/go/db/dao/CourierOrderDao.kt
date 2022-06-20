package ru.wb.go.db.dao

import androidx.room.*
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import kotlinx.coroutines.flow.Flow
import ru.wb.go.db.entity.courierlocal.*

@Dao
interface CourierOrderDao {

    //==============================================================================================
    //order
    //==============================================================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(orderEntity: CourierOrderLocalEntity)

    @Transaction
    @Query("SELECT * FROM CourierOrderLocalEntity")
    fun orderAndOffices(): Flow<List<CourierOrderLocalDataEntity>>

    @Transaction
    @Query("SELECT * FROM CourierOrderLocalEntity WHERE rowId=:rowOrder")
    suspend fun orderAndOffices(rowOrder: Int):  CourierOrderLocalDataEntity

    @Transaction
    @Query("SELECT * FROM CourierOrderLocalEntity")
    fun observeOrderData(): Flow<CourierOrderLocalDataEntity>

    @Query("DELETE FROM CourierOrderLocalEntity")
    suspend fun deleteAllOrder()

    // TODO: 15.09.2021 вынести в отдельный DAO
    //==============================================================================================
    //order dst offices
    //==============================================================================================
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrderOffices(courierOrderDstOfficeLocalEntity: List<CourierOrderDstOfficeLocalEntity>)

    @Query("DELETE FROM CourierOrderDstOfficeLocalEntity")
    suspend fun deleteAllOffices()

    // ====================================
    // True Local Order
    @Transaction
    suspend fun addOrderFromReserve(order: LocalOrderEntity) {
        addOrder(order)
        addOfficesFromReserve(order.orderId)
    }

    @Insert
    suspend fun addOrder(localOrder: LocalOrderEntity)

    @Query(
        """
        INSERT INTO offices(office_id, office_name, address, latitude, longitude, delivered_boxes, count_boxes, is_visited, is_online)
        SELECT dst_office_id, dst_office_name, dst_office_full_address, dst_office_latitude, dst_office_longitude,0,0,0,1
        FROM CourierOrderDstOfficeLocalEntity WHERE dst_office_order_id=:orderId
    """
    )
    suspend fun addOfficesFromReserve(orderId: Int)

    @Query("DELETE FROM courier_order")
    suspend fun deleteOrder()

    @Query("SELECT * FROM courier_order")
    suspend fun getOrder(): LocalOrderEntity

    @Insert
    suspend fun addOffices(offices: List<LocalOfficeEntity>)

    @Query("DELETE FROM offices")
    suspend fun deleteOffices()

    @Query("SELECT * FROM offices")
    suspend fun getOffices(): List<LocalOfficeEntity>

    @Query("SELECT * FROM offices WHERE office_id=:officeId")
    suspend fun getOfficeById(officeId: Int):  LocalOfficeEntity

    @Query("SELECT * FROM offices")
    fun getOfficesFlowable(): Flow<List<LocalOfficeEntity>>

    @Query("UPDATE courier_order SET status=:status, started_at=:startedAt")
    suspend fun setOrderStart(status: String, startedAt: String)

    @Query("DELETE FROM offices WHERE count_boxes=0")
    suspend fun deleteNotUsedOffices()

    @Query("UPDATE courier_order SET status=:status, cost=:cost")
    suspend fun setCost(status: String, cost: Int)

    @Transaction
    suspend fun setOrderAfterLoadStatus(status: String, cost: Int) {
        deleteNotUsedOffices()
        setCost(status, cost)
    }

    @Query("UPDATE offices SET is_visited = 1, is_online=0 WHERE office_id=:officeId")
    suspend fun setVisitOffice(officeId: Int)

    @Query("UPDATE offices SET is_online=1 ")
    suspend fun setOnlineOffice()
}