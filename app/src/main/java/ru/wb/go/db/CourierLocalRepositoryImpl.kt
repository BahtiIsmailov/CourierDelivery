package ru.wb.go.db

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
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

    override suspend fun saveCurrentWarehouse(courierWarehouseEntity: CourierWarehouseLocalEntity) {
        withContext(Dispatchers.IO) {
            courierWarehouseDao.insert(courierWarehouseEntity)
        }

    }

    override suspend fun readCurrentWarehouse(): CourierWarehouseLocalEntity {
        return withContext(Dispatchers.IO) {
            courierWarehouseDao.read()
        }
    }

    override suspend fun getOrderGate(): String {
        return getOrder().gate

    }

    override suspend fun deleteAllWarehouse() {
        withContext(Dispatchers.IO) {
            courierWarehouseDao.deleteAll()
        }

    }

    override suspend fun saveFreeOrders(courierOrderLocalDataEntities: List<CourierOrderLocalDataEntity>) {
        courierOrderLocalDataEntities.map {
            Log.e("method.call()", "saveFreeOrders start")
            saveOrderAndOffices(it.courierOrderLocalEntity, it.dstOffices)
            Log.e("method.call()", "saveFreeOrders end")
        }

    }


    override fun freeOrders(): Flow<List<CourierOrderLocalDataEntity>> {
        return courierOrderDao.orderAndOffices()
            .flowOn(Dispatchers.IO)
    }

    private suspend fun saveOrderAndOffices(
        courierOrderLocalEntity: CourierOrderLocalEntity,
        courierOrderDstOfficesLocalEntity: List<CourierOrderDstOfficeLocalEntity>
    ) {
        withContext(Dispatchers.IO) {
            courierOrderDao.insertOrder(courierOrderLocalEntity)
            courierOrderDao.insertOrderOffices(courierOrderDstOfficesLocalEntity)
        }
    }


    override suspend fun orderAndOffices(rowOrder: Int): CourierOrderLocalDataEntity {
        return withContext(Dispatchers.IO) {
            courierOrderDao.orderAndOffices(rowOrder)
        }
    }

    override suspend fun observeOrderData(): CourierOrderLocalDataEntity {
        return withContext(Dispatchers.IO) {
            courierOrderDao.observeOrderData()
        }
    }

    override suspend fun deleteAllOrder() {
        withContext(Dispatchers.IO) {
            courierOrderDao.deleteAllOrder()
        }

    }

    override suspend fun deleteAllOrderOffices() {
        withContext(Dispatchers.IO) {
            courierOrderDao.deleteAllOffices()
        }
    }

    override suspend fun findOfficeById(officeId: Int): LocalOfficeEntity {
        return withContext(Dispatchers.IO) {
            courierOrderDao.getOfficeById(officeId)
        }
    }

    override suspend fun getOrder(): LocalOrderEntity {
        return withContext(Dispatchers.IO) {
            courierOrderDao.getOrder()
        }
    }

    override suspend fun deleteOrder() {
        withContext(Dispatchers.IO) {
            courierLoadingBoxDao.deleteBoxes()
            courierOrderDao.deleteOffices()
            courierOrderDao.deleteOrder()
        }
    }

    override suspend fun saveRemoteOrder(
        order: LocalComplexOrderEntity,
        boxes: List<LocalBoxEntity>
    ) {
        withContext(Dispatchers.IO) {
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
    }

    override suspend fun setOrderOrderStart(scanTime: String) {
        withContext(Dispatchers.IO){
            courierOrderDao.setOrderStart(TaskStatus.STARTED.status, scanTime)
        }
    }

    override suspend fun getOrderId(): String {
        return withContext(Dispatchers.IO){
            getOrder().orderId.toString()
        }
    }

    override suspend fun setOrderInReserve(order: LocalOrderEntity) {
        withContext(Dispatchers.IO){
            courierOrderDao.addOrderFromReserve(order)
        }
    }

    override suspend fun setOrderAfterLoadStatus(cost: Int) {
        withContext(Dispatchers.IO){
            assert(cost != 0)
            courierOrderDao.setOrderAfterLoadStatus(TaskStatus.INTRANSIT.status, cost)
        }
    }

    override suspend fun clearOrder() {
        withContext(Dispatchers.IO) {
            courierOrderDao.deleteAllOffices()
            courierOrderDao.deleteAllOrder()
            courierWarehouseDao.deleteAll()
            deleteOrder()
        }
    }

    override suspend fun readAllLoadingBoxesSync(): List<LocalBoxEntity> {
        return withContext(Dispatchers.IO){
            courierLoadingBoxDao.readAllBoxesSync()
        }
    }

    override suspend fun loadingBoxBoxesGroupByOffice(): List<LocalLoadingBoxEntity> {
        return withContext(Dispatchers.IO){
            courierLoadingBoxDao.loadingBoxBoxesGroupByOffice()
        }
    }


    override suspend fun getOffices(): List<LocalOfficeEntity> {
        return withContext(Dispatchers.IO){
            courierOrderDao.getOffices()
        }
    }

    override suspend fun getOfficesFlowable(): List<LocalOfficeEntity> {
        return withContext(Dispatchers.IO){
            courierOrderDao.getOfficesFlowable()
        }
    }

    override suspend fun loadBoxOnboard(box: LocalBoxEntity, isNew: Boolean) {
        withContext(Dispatchers.IO){
            when (isNew) {
                true -> courierLoadingBoxDao.addNewBox(box)
                false -> courierLoadingBoxDao.updateBoxLoadingAt(box.boxId, box.loadingAt)
            }
        }
    }

    override suspend fun visitOffice(officeId: Int) {
        withContext(Dispatchers.IO) {
            courierOrderDao.setVisitOffice(officeId)
        }
    }

    override suspend fun getOfflineBoxes(): List<LocalBoxEntity> {
        return withContext(Dispatchers.IO) {
             courierLoadingBoxDao.getOfflineBoxes()
        }
    }

    override suspend fun setOnlineOffices() {
          withContext(Dispatchers.IO){
              courierOrderDao.setOnlineOffice()
          }
    }

    override suspend fun unloadBox(box: LocalBoxEntity) {
        withContext(Dispatchers.IO){
            courierLoadingBoxDao.unloadBoxInOffice(box)
        }
    }

    override suspend fun takeBackBox(box: LocalBoxEntity) {
        withContext(Dispatchers.IO) {
            courierLoadingBoxDao.takeBoxBack(box)
        }
    }

    override suspend fun getBoxesLiveData(): List<LocalBoxEntity> {
        return withContext(Dispatchers.IO){
            courierLoadingBoxDao.getBoxesLive()
        }
    }

    override suspend fun getBoxes(): List<LocalBoxEntity> {
         return withContext(Dispatchers.IO){
             courierLoadingBoxDao.getBoxes()
         }
    }

    override suspend fun getRemainBoxes(officeId: Int): List<LocalBoxEntity> {
        return withContext(Dispatchers.IO){
            courierLoadingBoxDao.getRemainBoxes(officeId)
        }
    }

}