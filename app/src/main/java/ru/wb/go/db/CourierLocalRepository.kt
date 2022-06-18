package ru.wb.go.db

import kotlinx.coroutines.flow.Flow
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.db.entity.courierlocal.*

interface CourierLocalRepository {

    //==============================================================================================
    //warehouse
    //==============================================================================================
    suspend fun saveCurrentWarehouse(courierWarehouseEntity: CourierWarehouseLocalEntity)

    suspend fun readCurrentWarehouse(): CourierWarehouseLocalEntity

    suspend fun deleteAllWarehouse()

    //==============================================================================================
    //order and offices
    //==============================================================================================

    suspend fun saveFreeOrders(courierOrderLocalDataEntities: List<CourierOrderLocalDataEntity>)

    fun freeOrders(): Flow<List<CourierOrderLocalDataEntity>>

    suspend fun orderAndOffices(rowOrder: Int): CourierOrderLocalDataEntity

    fun observeOrderData(): Flow<CourierOrderLocalDataEntity>

    suspend fun deleteAllOrder()

    suspend fun deleteAllOrderOffices()

    suspend fun findOfficeById(officeId: Int): LocalOfficeEntity

    //==============================
    // True Order
    //==============================

    suspend fun getOrder(): LocalOrderEntity

    suspend fun deleteOrder()

    suspend fun saveRemoteOrder(order: LocalComplexOrderEntity, boxes: List<LocalBoxEntity>)

    suspend fun setOrderOrderStart(scanTime: String)

    suspend fun setOrderInReserve(order: LocalOrderEntity)

    suspend fun setOrderAfterLoadStatus(cost: Int)

    suspend fun getOrderId(): String

    suspend fun getOrderGate(): String

    suspend fun getOffices(): List<LocalOfficeEntity>

    fun getOfficesFlowable(): Flow<List<LocalOfficeEntity>>

    suspend fun getBoxes(): List<LocalBoxEntity>

    fun getBoxesLiveData(): Flow<List<LocalBoxEntity>>

    suspend fun loadBoxOnboard(box: LocalBoxEntity, isNew: Boolean)

    suspend fun visitOffice(officeId: Int)

    suspend fun getOfflineBoxes(): List<LocalBoxEntity>

    suspend fun setOnlineOffices()

    suspend fun unloadBox(box: LocalBoxEntity)

    suspend fun takeBackBox(box: LocalBoxEntity)

    suspend fun readAllLoadingBoxesSync(): List<LocalBoxEntity>

    suspend fun loadingBoxBoxesGroupByOffice(): List<LocalLoadingBoxEntity>

    suspend fun clearOrder()

    suspend fun getRemainBoxes(officeId: Int): List<LocalBoxEntity>

}

/*


    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

    fun deleteAllOrder()

    fun deleteAllOrderOffices()

    fun findOfficeById(officeId: Int): Single<LocalOfficeEntity>

    //==============================
    // True Order
    //==============================

    fun getOrder(): LocalOrderEntity

    fun deleteOrder()

    fun saveRemoteOrder(order: LocalComplexOrderEntity, boxes: List<LocalBoxEntity>): Completable

    fun setOrderOrderStart(scanTime: String)

    fun setOrderInReserve(order: LocalOrderEntity)

    fun setOrderAfterLoadStatus(cost: Int)

    fun getOrderId(): Single<String>
    fun getOrderGate(): Single<String>
    fun getOffices(): List<LocalOfficeEntity>
    fun getOfficesFlowable(): Flowable<List<LocalOfficeEntity>>
    fun getBoxes(): List<LocalBoxEntity>
    fun getBoxesLiveData(): Flowable<List<LocalBoxEntity>>

    fun loadBoxOnboard(box: LocalBoxEntity, isNew: Boolean): Completable

    fun visitOffice(officeId: Int)

    fun getOfflineBoxes(): List<LocalBoxEntity>

    fun setOnlineOffices()

    fun unloadBox(box: LocalBoxEntity)

    fun takeBackBox(box: LocalBoxEntity)

    fun readAllLoadingBoxesSync(): Single<List<LocalBoxEntity>>

    fun loadingBoxBoxesGroupByOffice(): Single<List<LocalLoadingBoxEntity>>

    fun clearOrder()

    fun getRemainBoxes(officeId: Int): Maybe<List<LocalBoxEntity>>

}
 */