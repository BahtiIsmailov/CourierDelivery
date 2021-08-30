package ru.wb.perevozka.db

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalEntity

interface CourierLocalRepository {

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

    fun saveBox(courierBoxEntity: CourierBoxEntity): Completable

    fun saveBoxes(courierBoxesEntity: List<CourierBoxEntity>): Completable

    fun readAllBoxes(): Single<List<CourierBoxEntity>>

    fun observeBoxes(): Flowable<List<CourierBoxEntity>>

    fun deleteBox(courierBoxEntity: CourierBoxEntity): Completable

    fun deleteBoxes(courierBoxEntity: List<CourierBoxEntity>): Completable

    fun deleteBoxesByQrCode(qrCodes: List<String>): Completable

    fun deleteAllFlightBoxes()

}