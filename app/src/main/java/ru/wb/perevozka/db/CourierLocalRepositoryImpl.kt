package ru.wb.perevozka.db

import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import ru.wb.perevozka.db.dao.CourierBoxDao
import ru.wb.perevozka.db.dao.CourierOrderDao
import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalEntity

class CourierLocalRepositoryImpl(
    private val courierOrderDao: CourierOrderDao,
    private val courierBoxDao: CourierBoxDao
) : CourierLocalRepository {

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

    override fun saveBox(courierBoxEntity: CourierBoxEntity): Completable {
        return courierBoxDao.insertBox(courierBoxEntity)
    }

    override fun saveBoxes(courierBoxesEntity: List<CourierBoxEntity>): Completable {
        return courierBoxDao.insertBoxes(courierBoxesEntity)
    }

    override fun readAllBoxes(): Single<List<CourierBoxEntity>> {
        return courierBoxDao.readAllBoxes()
    }

    override fun observeBoxes(): Flowable<List<CourierBoxEntity>> {
        return courierBoxDao.observeBoxes()
    }

    override fun deleteBox(courierBoxEntity: CourierBoxEntity): Completable {
        return courierBoxDao.deleteBox(courierBoxEntity)
    }

    override fun deleteBoxes(courierBoxEntity: List<CourierBoxEntity>): Completable {
        return courierBoxDao.deleteBoxes(courierBoxEntity)
    }

    override fun deleteBoxesByQrCode(qrCodes: List<String>): Completable {
        return courierBoxDao.deleteBoxesByQrCode(qrCodes)
    }

    override fun deleteAllFlightBoxes() {
        return courierBoxDao.deleteAllBoxes()
    }

}