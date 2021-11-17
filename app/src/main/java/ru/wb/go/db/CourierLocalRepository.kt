package ru.wb.go.db

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.db.entity.courierboxes.CourierBoxEntity
import ru.wb.go.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity
import ru.wb.go.db.entity.courierlocal.*
import ru.wb.go.ui.courierintransit.domain.CompleteDeliveryResult
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingBoxCounterResult
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingInitLastBoxResult

interface CourierLocalRepository {

    //==============================================================================================
    //warehouse
    //==============================================================================================
    fun saveCurrentWarehouse(courierWarehouseEntity: CourierWarehouseLocalEntity): Completable

    fun readCurrentWarehouse(): Single<CourierWarehouseLocalEntity>

    fun courierTimerEntity(): Single<CourierTimerEntity>

    fun courierLoadingInfoEntity(): Single<CourierLoadingInfoEntity>

    fun deleteAllWarehouse()

    //==============================================================================================
    //order and offices
    //==============================================================================================

    fun saveWarehouseAndOrderAndOffices(
        courierWarehouseLocalEntity: CourierWarehouseLocalEntity,
        courierOrderLocalEntity: CourierOrderLocalEntity,
        courierOrderDstOfficesLocalEntity: List<CourierOrderDstOfficeLocalEntity>,
    ): Completable

    fun saveOrderAndOffices(
        courierOrderLocalEntity: CourierOrderLocalEntity,
        courierOrderDstOfficesLocalEntity: List<CourierOrderDstOfficeLocalEntity>,
    ): Completable

    fun orderData(): Single<CourierOrderLocalDataEntity>

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

    fun deleteAllOrder()

    fun deleteAllOrderOffices()

    fun updateVisitedAtOffice(officeId: Int, visitedAt: String): Completable

    fun updateVisitedOfficeByBoxes(): Completable

    fun insertVisitedOffice(courierOrderVisitedOfficeLocalEntity: CourierOrderVisitedOfficeLocalEntity): Completable

    fun insertAllVisitedOffice(): Completable

    fun findOfficeById(officeId: Int): Single<CourierOrderDstOfficeLocalEntity>

    //==============================================================================================
    //boxes
    //==============================================================================================

    fun saveLoadingBox(boxEntity: CourierBoxEntity): Completable

    fun saveLoadingBoxes(boxEntity: List<CourierBoxEntity>): Completable

    fun readAllLoadingBoxes(): Single<List<CourierBoxEntity>>

    fun readAllLoadingBoxesByOfficeId(officeId: Int): Single<List<CourierBoxEntity>>

    fun readAllUnloadingBoxesByOfficeId(officeId: Int): Single<List<CourierBoxEntity>>

    fun readInitLastUnloadingBox(officeId: Int): Single<CourierUnloadingInitLastBoxResult>

    fun readNotUnloadingBoxes(): Single<List<CourierBoxEntity>>

    fun deleteAllVisitedOffices()

    fun readUnloadingBoxCounter(officeId: Int): Single<CourierUnloadingBoxCounterResult>

    fun observeUnloadingBoxCounter(officeId: Int): Flowable<CourierUnloadingBoxCounterResult>

    fun observeLoadingBoxes(): Flowable<List<CourierBoxEntity>>

    fun deleteLoadingBox(boxEntity: CourierBoxEntity): Completable

    fun deleteLoadingBoxes(boxEntity: List<CourierBoxEntity>): Completable

    fun deleteLoadingBoxesByQrCode(qrCodes: List<String>): Completable

    fun deleteAllLoadingBoxes()

    fun observeBoxesGroupByOffice(): Flowable<List<CourierIntransitGroupByOfficeEntity>>

    fun completeDeliveryResult(): Single<CompleteDeliveryResult>


}