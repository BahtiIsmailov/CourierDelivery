package ru.wb.go.db

import io.reactivex.Completable
import io.reactivex.Flowable
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

    fun saveOrderAndOffices(
        courierOrderLocalEntity: CourierOrderLocalEntity,
        courierOrderDstOfficesLocalEntity: List<CourierOrderDstOfficeLocalEntity>,
    ): Completable

    fun orderData(): CourierOrderLocalDataEntity?


    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

    fun deleteAllOrder()

    fun deleteAllOrderOffices()

    fun findOfficeById(officeId: Int): Single<LocalOfficeEntity>

    //==============================
    // True Order
    //==============================

    fun getOrder(): LocalOrderEntity?

    fun deleteOrder()

    fun saveRemoteOrder(order: LocalComplexOrderEntity, boxes: List<LocalBoxEntity>): Completable

    fun setOrderOrderStart()

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

    fun clearOrder()


}