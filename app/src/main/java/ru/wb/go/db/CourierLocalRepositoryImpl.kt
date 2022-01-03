package ru.wb.go.db

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import ru.wb.go.db.dao.CourierBoxDao
import ru.wb.go.db.dao.CourierOrderDao
import ru.wb.go.db.dao.CourierWarehouseDao
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.db.entity.courierboxes.CourierBoxEntity
import ru.wb.go.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity
import ru.wb.go.db.entity.courierlocal.*
import ru.wb.go.ui.courierintransit.domain.CompleteDeliveryResult
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingBoxScoreResult
import ru.wb.go.ui.courierunloading.domain.CourierUnloadingInitLastBoxResult

class CourierLocalRepositoryImpl(
    private val courierWarehouseDao: CourierWarehouseDao,
    private val courierOrderDao: CourierOrderDao,
    private val courierLoadingBoxDao: CourierBoxDao
) : CourierLocalRepository {

    override fun saveCurrentWarehouse(courierWarehouseEntity: CourierWarehouseLocalEntity): Completable {
        return courierWarehouseDao.insert(courierWarehouseEntity)
    }

    override fun readCurrentWarehouse(): Single<CourierWarehouseLocalEntity> {
        return courierWarehouseDao.read()
    }

    override fun courierTimerEntity(): Single<CourierTimerEntity> {
        return courierWarehouseDao.courierTimerEntity()
    }

    override fun courierLoadingInfoEntity(): Single<CourierLoadingInfoEntity> {
        return courierWarehouseDao.courierLoadingInfoEntity()
    }

    override fun deleteAllWarehouse() {
        courierWarehouseDao.deleteAll()
    }

    override fun saveWarehouseAndOrderAndOffices(
        courierWarehouseLocalEntity: CourierWarehouseLocalEntity,
        courierOrderLocalEntity: CourierOrderLocalEntity,
        courierOrderDstOfficesLocalEntity: List<CourierOrderDstOfficeLocalEntity>,
    ): Completable {
        return courierWarehouseDao.insert(courierWarehouseLocalEntity)
            .andThen(courierOrderDao.insertOrder(courierOrderLocalEntity))
            .andThen(courierOrderDao.insertOrderOffices(courierOrderDstOfficesLocalEntity))
    }

    override fun saveOrderAndOffices(
        courierOrderLocalEntity: CourierOrderLocalEntity,
        courierOrderDstOfficesLocalEntity: List<CourierOrderDstOfficeLocalEntity>
    ): Completable {
        return courierOrderDao.insertOrder(courierOrderLocalEntity)
            .andThen(courierOrderDao.insertOrderOffices(courierOrderDstOfficesLocalEntity))
    }

    override fun orderDataSync(): Single<CourierOrderLocalDataEntity> {
        return courierOrderDao.orderDataSync()
    }

    override fun orderData(): CourierOrderLocalDataEntity {
        return courierOrderDao.orderData()
    }

    override fun observeOrderData(): Flowable<CourierOrderLocalDataEntity> {
        return courierOrderDao.observeOrderData()
    }

    override fun deleteAllOrder() {
        courierOrderDao.deleteAllOrder()
    }

    override fun deleteAllOrderOffices() {
        courierOrderDao.deleteAllOffices()
    }

    override fun updateVisitedAtOffice(officeId: Int, visitedAt: String): Completable {
        return courierOrderDao.updateVisitedAtOffice(officeId, visitedAt)
    }

    override fun updateVisitedOfficeByBoxes(): Completable {
        return courierOrderDao.updateVisitedOfficeByBoxes()
    }

    override fun insertVisitedOfficeSync(courierOrderVisitedOfficeLocalEntity: CourierOrderVisitedOfficeLocalEntity): Completable {
        return courierOrderDao.insertVisitedOfficeSync(courierOrderVisitedOfficeLocalEntity)
    }

    override fun insertVisitedOffice(courierOrderVisitedOfficeLocalEntity: CourierOrderVisitedOfficeLocalEntity) {
        return courierOrderDao.insertVisitedOffice(courierOrderVisitedOfficeLocalEntity)
    }

    override fun insertAllVisitedOfficeSync(): Completable {
        return courierOrderDao.insertAllVisitedOfficeSync()
    }

    override fun insertAllVisitedOffice() {
        return courierOrderDao.insertAllVisitedOffice()
    }

    override fun findOfficeById(officeId: Int): Single<CourierOrderDstOfficeLocalEntity> {
        return courierOrderDao.findOfficeById(officeId)
    }

    override fun saveLoadingBox(boxEntity: CourierBoxEntity): Completable {
        return courierLoadingBoxDao.insertBox(boxEntity)
    }

    override fun findLoadingBoxById(id: String): Maybe<CourierBoxEntity> {
        return courierLoadingBoxDao.findLoadingBoxById(id)
    }

    override fun saveLoadingBoxes(boxEntity: List<CourierBoxEntity>): Completable {
        return courierLoadingBoxDao.insertBoxes(boxEntity)
    }

    override fun readAllLoadingBoxesSync(): Single<List<CourierBoxEntity>> {
        return courierLoadingBoxDao.readAllBoxesSync()
    }

    override fun readAllLoadingBoxes(): List<CourierBoxEntity> {
        return courierLoadingBoxDao.readAllBoxes()
    }

    override fun readAllLoadingBoxesByOfficeId(officeId: Int): Single<List<CourierBoxEntity>> {
        return courierLoadingBoxDao.readAllLoadingBoxesByOfficeId(officeId)
    }

    override fun readLoadingBoxByOfficeIdAndId(
        officeId: Int,
        id: String
    ): Maybe<CourierBoxEntity> {
        return courierLoadingBoxDao.readLoadingBoxByOfficeIdAndId(officeId, id)
    }

    override fun readAllUnloadingBoxesByOfficeId(officeId: Int): Single<List<CourierBoxEntity>> {
        return courierLoadingBoxDao.readAllUnloadingBoxesByOfficeId(officeId)
    }

    override fun readInitLastUnloadingBox(officeId: Int): Single<CourierUnloadingInitLastBoxResult> {
        return courierLoadingBoxDao.readInitLastUnloadingBox(officeId)
            .onErrorReturn { CourierUnloadingInitLastBoxResult("", "") }
    }

    override fun readNotUnloadingBoxesSync(): Single<List<CourierBoxEntity>> {
        return courierLoadingBoxDao.readNotUnloadingBoxesSync()
    }

    override fun readNotUnloadingBoxes(): List<CourierBoxEntity> {
        return courierLoadingBoxDao.readNotUnloadingBoxes()
    }

    override fun deleteAllVisitedOffices() {
        courierLoadingBoxDao.deleteAllVisitedOffices()
    }

    override fun readUnloadingBoxCounter(officeId: Int): Single<CourierUnloadingBoxScoreResult> {
        return courierLoadingBoxDao.readUnloadingBoxCounter(officeId)
    }

    override fun observeUnloadingBoxCounter(officeId: Int): Flowable<CourierUnloadingBoxScoreResult> {
        return courierLoadingBoxDao.observeCounterBox(officeId)
    }

    override fun observeLoadingBoxes(): Flowable<List<CourierBoxEntity>> {
        return courierLoadingBoxDao.observeBoxes()
    }

    override fun deleteLoadingBox(boxEntity: CourierBoxEntity): Completable {
        return courierLoadingBoxDao.deleteBox(boxEntity)
    }

    override fun deleteLoadingBoxes(boxEntity: List<CourierBoxEntity>): Completable {
        return courierLoadingBoxDao.deleteBoxes(boxEntity)
    }

    override fun deleteLoadingBoxesByQrCode(qrCodes: List<String>): Completable {
        return courierLoadingBoxDao.deleteBoxesByQrCode(qrCodes)
    }

    override fun deleteAllLoadingBoxes() {
        return courierLoadingBoxDao.deleteAllBoxes()
    }

    override fun observeBoxesGroupByOffice(): Flowable<List<CourierIntransitGroupByOfficeEntity>> {
        return courierLoadingBoxDao.observeBoxesGroupByOffice()
    }

    override fun completeDeliveryResult(): Single<CompleteDeliveryResult> {
        return courierLoadingBoxDao.completeDeliveryResult()
    }

}