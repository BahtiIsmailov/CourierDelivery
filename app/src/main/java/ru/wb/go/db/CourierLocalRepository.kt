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
    suspend fun saveCurrentWarehouse(courierWarehouseEntity: CourierWarehouseLocalEntity)

    fun readCurrentWarehouse():  CourierWarehouseLocalEntity

    fun deleteAllWarehouse()

    //==============================================================================================
    //order and offices
    //==============================================================================================

    suspend fun saveFreeOrders(courierOrderLocalDataEntities: List<CourierOrderLocalDataEntity>)

    suspend fun freeOrders():  List<CourierOrderLocalDataEntity>

    suspend fun orderAndOffices(rowOrder: Int):  CourierOrderLocalDataEntity

    suspend fun observeOrderData(): CourierOrderLocalDataEntity

    fun deleteAllOrder()

    fun deleteAllOrderOffices()

    fun findOfficeById(officeId: Int): LocalOfficeEntity

    //==============================
    // True Order
    //==============================

    suspend fun getOrder(): LocalOrderEntity

    fun deleteOrder()

    suspend fun saveRemoteOrder(order: LocalComplexOrderEntity, boxes: List<LocalBoxEntity>)

    fun setOrderOrderStart(scanTime: String)

    fun setOrderInReserve(order: LocalOrderEntity)

    fun setOrderAfterLoadStatus(cost: Int)

    suspend fun getOrderId():  String
    suspend fun getOrderGate():  String
    fun getOffices(): List<LocalOfficeEntity>
    suspend fun getOfficesFlowable():  List<LocalOfficeEntity>
    fun getBoxes(): List<LocalBoxEntity>
    suspend fun getBoxesLiveData():  List<LocalBoxEntity>

    fun loadBoxOnboard(box: LocalBoxEntity, isNew: Boolean)

    fun visitOffice(officeId: Int)

    fun getOfflineBoxes(): List<LocalBoxEntity>

    fun setOnlineOffices()

    fun unloadBox(box: LocalBoxEntity)

    fun takeBackBox(box: LocalBoxEntity)

    suspend fun readAllLoadingBoxesSync():  List<LocalBoxEntity>

    suspend fun loadingBoxBoxesGroupByOffice(): List<LocalLoadingBoxEntity>

    fun clearOrder()

    suspend fun getRemainBoxes(officeId: Int): List<LocalBoxEntity>

}