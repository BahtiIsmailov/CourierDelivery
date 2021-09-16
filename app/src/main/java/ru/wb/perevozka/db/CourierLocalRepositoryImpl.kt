package ru.wb.perevozka.db

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.perevozka.db.dao.CourierBoxDao
import ru.wb.perevozka.db.dao.CourierOrderDao
import ru.wb.perevozka.db.dao.CourierWarehouseDao
import ru.wb.perevozka.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity
import ru.wb.perevozka.db.entity.courierboxes.CourierIntransitGroupByOfficeEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierTimerEntity

class CourierLocalRepositoryImpl(
    private val courierWarehouseDao: CourierWarehouseDao,
    private val courierOrderDao: CourierOrderDao,
    private val courierLoadingBoxDao: CourierBoxDao,
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

    override fun deleteAllCurrentWarehouse() {
        courierWarehouseDao.deleteAll()
    }

    override fun saveCurrentOrderAndOffices(
        courierOrderLocalEntity: CourierOrderLocalEntity,
        courierOrderDstOfficesLocalEntity: List<CourierOrderDstOfficeLocalEntity>,
    ): Completable {
        return courierOrderDao.insertOrder(courierOrderLocalEntity)
            .andThen(courierOrderDao.insertOrderOffices(courierOrderDstOfficesLocalEntity))
    }

    override fun orderData(): Single<CourierOrderLocalDataEntity> {
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

    override fun saveLoadingBox(boxEntity: CourierBoxEntity): Completable {
        return courierLoadingBoxDao.insertBox(boxEntity)
    }

    override fun saveLoadingBoxes(boxEntity: List<CourierBoxEntity>): Completable {
        return courierLoadingBoxDao.insertBoxes(boxEntity)
    }

    override fun readAllLoadingBoxes(): Single<List<CourierBoxEntity>> {
        return courierLoadingBoxDao.readAllBoxes()
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

}