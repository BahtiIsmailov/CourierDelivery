package ru.wb.go.db

import io.reactivex.*
import ru.wb.go.db.dao.CourierBoxDao
import ru.wb.go.db.dao.CourierOrderDao
import ru.wb.go.db.dao.CourierWarehouseDao
import ru.wb.go.db.entity.TaskStatus
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.db.entity.courierlocal.*

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

    override fun getOrderGate(): Single<String> {
        return Single.just(getOrder()!!.gate)
    }

    override fun deleteAllWarehouse() {
        courierWarehouseDao.deleteAll()
    }

    override suspend fun saveFreeOrders(courierOrderLocalDataEntities: List<CourierOrderLocalDataEntity>) {
        courierOrderLocalDataEntities.map {
            saveOrderAndOffices(it.courierOrderLocalEntity, it.dstOffices)
        }
    }


    override fun freeOrders(): Single<List<CourierOrderLocalDataEntity>> {
        return courierOrderDao.orderAndOffices()
    }
    private suspend fun saveOrderAndOffices(
        courierOrderLocalEntity: CourierOrderLocalEntity,
        courierOrderDstOfficesLocalEntity: List<CourierOrderDstOfficeLocalEntity>
    )  {
          courierOrderDao.insertOrder(courierOrderLocalEntity)
          courierOrderDao.insertOrderOffices(courierOrderDstOfficesLocalEntity)
    }

    override fun orderAndOffices(rowOrder: Int): Single<CourierOrderLocalDataEntity> {
        return courierOrderDao.orderAndOffices(rowOrder)
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

    override fun findOfficeById(officeId: Int): Single<LocalOfficeEntity> {
        return courierOrderDao.getOfficeById(officeId)
    }

    override fun getOrder(): LocalOrderEntity {
        return courierOrderDao.getOrder()
    }

    override fun deleteOrder() {
        courierLoadingBoxDao.deleteBoxes()
        courierOrderDao.deleteOffices()
        courierOrderDao.deleteOrder()
    }

    override fun saveRemoteOrder(
        order: LocalComplexOrderEntity,
        boxes: List<LocalBoxEntity>
    ): Completable {
        assert(order.order.status != "") { "Absent status" }
        val bg = boxes.groupingBy { it.officeId }.fold(Pair(0, 0)) { t, o ->
            Pair(t.first + 1, t.second + if (o.deliveredAt.isNotEmpty()) 1 else 0)
        }

        var offices = order.offices.map {
            val cb = bg[it.officeId]?.first ?: 0
            val db = bg[it.officeId]?.second ?: 0
            it.copy(countBoxes = cb, deliveredBoxes = db, isVisited = db > 0, isOnline = true)
        }

        if (order.order.status == TaskStatus.INTRANSIT.status) {
            offices = offices.filter { o -> o.countBoxes > 0 }
        }

//FIXME Нужно в транзакцию запихнуть
        courierOrderDao.addOrder(order.order)
        courierOrderDao.addOffices(offices)
        courierLoadingBoxDao.addBoxes(boxes)
        return Completable.complete()
    }

    override fun setOrderOrderStart(scanTime: String) {
        courierOrderDao.setOrderStart(TaskStatus.STARTED.status, scanTime)
    }

    override fun getOrderId(): Single<String> {
        return Single.just(getOrder()!!.orderId.toString())
    }

    override fun setOrderInReserve(order: LocalOrderEntity) {
        courierOrderDao.addOrderFromReserve(order)
    }

    override fun setOrderAfterLoadStatus(cost: Int) {
        assert(cost != 0)
        courierOrderDao.setOrderAfterLoadStatus(TaskStatus.INTRANSIT.status, cost)
    }

    override fun clearOrder() {
        courierOrderDao.deleteAllOffices()
        courierOrderDao.deleteAllOrder()
        courierWarehouseDao.deleteAll()
        deleteOrder()
    }

    override fun readAllLoadingBoxesSync(): Single<List<LocalBoxEntity>> {
        return courierLoadingBoxDao.readAllBoxesSync()
    }

    override fun loadingBoxBoxesGroupByOffice(): Single<List<LocalLoadingBoxEntity>> {
        return courierLoadingBoxDao.loadingBoxBoxesGroupByOffice()
    }


    override fun getOffices(): List<LocalOfficeEntity> {
        return courierOrderDao.getOffices()
    }

    override fun getOfficesFlowable(): Flowable<List<LocalOfficeEntity>> {
        return courierOrderDao.getOfficesFlowable()
    }

    override fun loadBoxOnboard(box: LocalBoxEntity, isNew: Boolean): Completable {
        when (isNew) {
            true -> courierLoadingBoxDao.addNewBox(box)
            false -> courierLoadingBoxDao.updateBoxLoadingAt(box.boxId, box.loadingAt)
        }
        return Completable.complete()
    }

    override fun visitOffice(officeId: Int) {
        courierOrderDao.setVisitOffice(officeId)
    }

    override fun getOfflineBoxes(): List<LocalBoxEntity> {
        return courierLoadingBoxDao.getOfflineBoxes()
    }

    override fun setOnlineOffices() {
        return courierOrderDao.setOnlineOffice()
    }

    override fun unloadBox(box: LocalBoxEntity) {
        courierLoadingBoxDao.unloadBoxInOffice(box)
    }

    override fun takeBackBox(box: LocalBoxEntity) {
        courierLoadingBoxDao.takeBoxBack(box)
    }

    override fun getBoxesLiveData(): Flowable<List<LocalBoxEntity>> {
        return courierLoadingBoxDao.getBoxesLive()
    }

    override fun getBoxes(): List<LocalBoxEntity> {
        return courierLoadingBoxDao.getBoxes()
    }

    override fun getRemainBoxes(officeId: Int): Maybe<List<LocalBoxEntity>> {
        return courierLoadingBoxDao.getRemainBoxes(officeId)
    }

}