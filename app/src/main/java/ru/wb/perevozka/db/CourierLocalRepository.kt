package ru.wb.perevozka.db

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity
import ru.wb.perevozka.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierTimerEntity

interface CourierLocalRepository {

    //==============================================================================================
    //warehouse
    //==============================================================================================
    fun saveCurrentWarehouse(courierWarehouseEntity: CourierWarehouseLocalEntity): Completable

    fun readCurrentWarehouse(): Single<CourierWarehouseLocalEntity>

    fun courierTimerEntity(): Single<CourierTimerEntity>

    fun deleteAllCurrentWarehouse()

    //==============================================================================================
    //order
    //==============================================================================================

    fun saveCurrentOrderAndOffices(
        courierOrderLocalEntity: CourierOrderLocalEntity,
        courierOrderDstOfficesLocalEntity: List<CourierOrderDstOfficeLocalEntity>,
    ): Completable

    fun orderData(): Single<CourierOrderLocalDataEntity>

    fun observeOrderData(): Flowable<CourierOrderLocalDataEntity>

    fun deleteAllOrder()

    fun deleteAllOrderOffices()

    //==============================================================================================
    //boxes
    //==============================================================================================

    fun saveLoadingBox(boxEntity: CourierBoxEntity): Completable

    fun saveLoadingBoxes(boxEntity: List<CourierBoxEntity>): Completable

    fun readAllLoadingBoxes(): Single<List<CourierBoxEntity>>

    fun observeLoadingBoxes(): Flowable<List<CourierBoxEntity>>

    fun deleteLoadingBox(boxEntity: CourierBoxEntity): Completable

    fun deleteLoadingBoxes(boxEntity: List<CourierBoxEntity>): Completable

    fun deleteLoadingBoxesByQrCode(qrCodes: List<String>): Completable

    fun deleteAllLoadingBoxes()

    fun observeBoxesGroupByOffice(): Flowable<List<CourierIntransitGroupByOfficeEntity>>


}