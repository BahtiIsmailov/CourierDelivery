package ru.wb.go.db

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.db.entity.courierlocal.*

interface CourierLocalRepository {

    //==============================================================================================
    //warehouse
    //==============================================================================================
    fun saveCurrentWarehouse(courierWarehouseEntity: CourierWarehouseLocalEntity): Completable

    fun readCurrentWarehouse(): Single<CourierWarehouseLocalEntity>

    fun deleteAllWarehouse()

    //==============================================================================================
    //order and offices
    //==============================================================================================

    suspend fun saveFreeOrders(courierOrderLocalDataEntities: List<CourierOrderLocalDataEntity>)

    fun freeOrders(): Single<List<CourierOrderLocalDataEntity>>

    fun orderAndOffices(rowOrder: Int): Single<CourierOrderLocalDataEntity>

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

    fun deleteAllOrder()

    fun deleteAllOrderOffices()

    fun findOfficeById(officeId: Int): Single<LocalOfficeEntity>

    //==============================
    // True Order
    //==============================

    suspend fun getOrder(): LocalOrderEntity

    fun deleteOrder()

    fun saveRemoteOrder(order: LocalComplexOrderEntity, boxes: List<LocalBoxEntity>): Completable

    fun setOrderOrderStart(scanTime: String)

    fun setOrderInReserve(order: LocalOrderEntity)

    fun setOrderAfterLoadStatus(cost: Int)

    suspend fun getOrderId():  String
    suspend fun getOrderGate():  String
    fun getOffices(): List<LocalOfficeEntity>
    fun getOfficesFlowable(): Flowable<List<LocalOfficeEntity>>
    fun getBoxes(): List<LocalBoxEntity>
    fun getBoxesLiveData(): Flowable<List<LocalBoxEntity>>

    fun loadBoxOnboard(box: LocalBoxEntity, isNew: Boolean)

    fun visitOffice(officeId: Int)

    fun getOfflineBoxes(): List<LocalBoxEntity>

    fun setOnlineOffices()

    fun unloadBox(box: LocalBoxEntity)

    fun takeBackBox(box: LocalBoxEntity)

    suspend fun readAllLoadingBoxesSync():  List<LocalBoxEntity>

    fun loadingBoxBoxesGroupByOffice(): Single<List<LocalLoadingBoxEntity>>

    fun clearOrder()

    fun getRemainBoxes(officeId: Int): Maybe<List<LocalBoxEntity>>

}