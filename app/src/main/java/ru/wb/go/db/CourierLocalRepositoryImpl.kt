package ru.wb.go.db

import android.util.Log
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
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

    override fun saveCurrentWarehouse(courierWarehouseEntity: CourierWarehouseLocalEntity) {
        courierWarehouseDao.insert(courierWarehouseEntity)
    }

    override fun readCurrentWarehouse(): CourierWarehouseLocalEntity {
        return courierWarehouseDao.read()
    }

    override fun getOrderGate(): String {
        return getOrder().gate
    }

    override fun deleteAllWarehouse() {
        courierWarehouseDao.deleteAll()
    }

    override suspend fun saveFreeOrders(courierOrderLocalDataEntities: List<CourierOrderLocalDataEntity>) {
        courierOrderLocalDataEntities.map {
            Log.e("method.call()","saveFreeOrders start")
            saveOrderAndOffices(it.courierOrderLocalEntity, it.dstOffices)
            Log.e("method.call()","saveFreeOrders end")
        }
    }


    override fun freeOrders(): Flow<List<CourierOrderLocalDataEntity>> {
        return courierOrderDao.orderAndOffices()
    }

    private suspend fun saveOrderAndOffices(
        courierOrderLocalEntity: CourierOrderLocalEntity,
        courierOrderDstOfficesLocalEntity: List<CourierOrderDstOfficeLocalEntity>
    ) {
        Log.e("method.call()","saveOrderAndOffices ")
//        delay(500)
        courierOrderDao.insertOrder(courierOrderLocalEntity)
        courierOrderDao.insertOrderOffices(courierOrderDstOfficesLocalEntity)
    }


    override fun orderAndOffices(rowOrder: Int): CourierOrderLocalDataEntity {
        return courierOrderDao.orderAndOffices(rowOrder)
    }

    override suspend fun observeOrderData(): CourierOrderLocalDataEntity {
        return courierOrderDao.observeOrderData()
    }

    override fun deleteAllOrder() {
        courierOrderDao.deleteAllOrder()
    }

    override fun deleteAllOrderOffices() {
        courierOrderDao.deleteAllOffices()
    }

    override fun findOfficeById(officeId: Int): LocalOfficeEntity {
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

    override suspend fun saveRemoteOrder(
        order: LocalComplexOrderEntity,
        boxes: List<LocalBoxEntity>
    ) {
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
    }

    override fun setOrderOrderStart(scanTime: String) {
        courierOrderDao.setOrderStart(TaskStatus.STARTED.status, scanTime)
    }

    override fun getOrderId(): String {
        return getOrder().orderId.toString()
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

    override fun readAllLoadingBoxesSync(): List<LocalBoxEntity> {
        return courierLoadingBoxDao.readAllBoxesSync()
    }

    override suspend fun loadingBoxBoxesGroupByOffice(): List<LocalLoadingBoxEntity> {
        return courierLoadingBoxDao.loadingBoxBoxesGroupByOffice()
    }


    override fun getOffices(): List<LocalOfficeEntity> {
        return courierOrderDao.getOffices()
    }

    override suspend fun getOfficesFlowable(): List<LocalOfficeEntity> {
        return courierOrderDao.getOfficesFlowable()
    }

    override fun loadBoxOnboard(box: LocalBoxEntity, isNew: Boolean) {
        when (isNew) {
            true -> courierLoadingBoxDao.addNewBox(box)
            false -> courierLoadingBoxDao.updateBoxLoadingAt(box.boxId, box.loadingAt)
        }
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

    override suspend fun getBoxesLiveData(): List<LocalBoxEntity> {
        return courierLoadingBoxDao.getBoxesLive()
    }

    override fun getBoxes(): List<LocalBoxEntity> {
        return courierLoadingBoxDao.getBoxes()
    }

    override suspend fun getRemainBoxes(officeId: Int): List<LocalBoxEntity> {
        return courierLoadingBoxDao.getRemainBoxes(officeId)
    }

}