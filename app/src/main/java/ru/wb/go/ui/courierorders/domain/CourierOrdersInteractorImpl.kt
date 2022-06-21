package ru.wb.go.ui.courierorders.domain

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.wb.go.app.AppPreffsKeys.SELECTED_ORDER_INDEX_KEY
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.TaskStatus
import ru.wb.go.db.entity.courier.CourierOrderDstOfficeEntity
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.db.entity.courier.CourierWarehouseLocalEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalDataEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.go.db.entity.courierlocal.LocalOrderEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.AppTasksRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.TokenManager
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.BaseServiceInteractorImpl
import ru.wb.go.ui.couriercarnumber.replaceCarNumberY
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.domain.CourierMapRepository
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.TimeManager
import ru.wb.go.utils.prefs.SharedWorker
import java.util.*

class CourierOrdersInteractorImpl(
    rxSchedulerFactory: RxSchedulerFactory,
    networkMonitorRepository: NetworkMonitorRepository,
    deviceManager: DeviceManager,
    private val appTasksRepository: AppTasksRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val courierLocalRepository: CourierLocalRepository,
    private val courierMapRepository: CourierMapRepository,
    private val userManager: UserManager,
    private val tokenManager: TokenManager,
    private val timeManager: TimeManager,
    private val sharedWorker: SharedWorker
) : BaseServiceInteractorImpl(rxSchedulerFactory, networkMonitorRepository, deviceManager),
    CourierOrdersInteractor {

    private fun List<CourierOrderDstOfficeEntity>.sortByUnusualTimeAndAddress(): List<CourierOrderDstOfficeEntity> {
        return this.sortedWith(
            compareBy({ !it.isUnusualTime }, { it.fullAddress.lowercase(Locale.ROOT) })
        )
    }

    override suspend fun freeOrdersLocalClearAndSave(srcOfficeID: Int): List<CourierOrderLocalDataEntity> {
        val response = appTasksRepository.getFreeOrders(srcOfficeID).sortedBy { it.id }

        response.forEach { freeOrders ->
            freeOrders.dstOffices = freeOrders.dstOffices.sortByUnusualTimeAndAddress()
        }
        courierLocalRepository.deleteAllOrder()
        courierLocalRepository.deleteAllOrderOffices()
        val localEntity = toCourierOrderLocalDataEntities(response)
        courierLocalRepository.saveFreeOrders(localEntity)
        return localEntity
    }



    override fun freeOrdersLocal(): Flow<MutableList<CourierOrderLocalDataEntity>> {
        return courierLocalRepository.freeOrders()
            .map { it.toMutableList() }
            .onEach {  }
    }

    /*
        override fun freeOrdersLocalClearAndSave(srcOfficeID: Int): Single<MutableList<CourierOrderLocalDataEntity>> {
        return appTasksRepository.getFreeOrders(srcOfficeID)
            .map { freeOrders ->
                val sortedFreeOrders = freeOrders.sortedBy { it.id }
                sortedFreeOrders.forEach {
                    it.dstOffices = it.dstOffices.sortByUnusualTimeAndAddress()
                }
                sortedFreeOrders
            }
            .flatMap {
                courierLocalRepository.deleteAllOrder()
                courierLocalRepository.deleteAllOrderOffices()
                val localEntity = toCourierOrderLocalDataEntities(it)
                courierLocalRepository.saveFreeOrders(localEntity)
                    .andThen(Single.just(localEntity))
            }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }
     */

//    override fun freeOrdersLocal(): Single<MutableList<CourierOrderLocalDataEntity>> {
//        return courierLocalRepository.freeOrders()
//            .map { it.toMutableList() }
//            .compose(rxSchedulerFactory.applySingleSchedulers())
//    }

    private fun toCourierOrderLocalDataEntities(it: List<CourierOrderEntity>): List<CourierOrderLocalDataEntity> {

        val courierOrderLocalDataEntities = mutableListOf<CourierOrderLocalDataEntity>()
        it.forEachIndexed { index, order ->
            val courierOrderLocalEntity = convertCourierOrderLocalEntity(order, index)
            val courierOrderDstOfficesLocalEntity =
                convertCourierOrderDstOfficesLocalEntity(order.dstOffices, order.id)
            courierOrderLocalDataEntities.add(
                CourierOrderLocalDataEntity(
                    courierOrderLocalEntity,
                    courierOrderDstOfficesLocalEntity
                )
            )
        }
        return courierOrderLocalDataEntities

    }

    override fun saveRowOrder(rowOrder: Int) {
        sharedWorker.save(SELECTED_ORDER_INDEX_KEY, rowOrder)
    }

    override fun selectedRowOrder(): Int {
        return sharedWorker.load(SELECTED_ORDER_INDEX_KEY, 0)

    }

    private fun convertCourierOrderDstOfficesLocalEntity(
        courierOrderDstOfficeEntities: List<CourierOrderDstOfficeEntity>,
        orderId: Int
    ): MutableList<CourierOrderDstOfficeLocalEntity> {
        val courierOrderDstOfficesLocalEntity = mutableListOf<CourierOrderDstOfficeLocalEntity>()
        courierOrderDstOfficeEntities.forEach {
            with(it) {
                courierOrderDstOfficesLocalEntity.add(
                    CourierOrderDstOfficeLocalEntity(
                        key = 0,
                        id = id,
                        orderId = orderId,
                        name = name,
                        fullAddress = fullAddress,
                        longitude = long,
                        latitude = lat,
                        visitedAt = "",
                        workTimes = workTimes,
                        isUnusualTime = isUnusualTime
                    )
                )
            }
        }
        return courierOrderDstOfficesLocalEntity
    }

    private fun convertCourierOrderLocalEntity(
        courierOrderEntity: CourierOrderEntity,
        rowId: Int
    ) = with(courierOrderEntity) {
        CourierOrderLocalEntity(
            id = id,
            rowId = rowId,
            routeID = routeID,
            gate = gate,
            minPrice = minPrice,
            minVolume = minVolume,
            minBoxesCount = minBoxesCount,
            reservedDuration = reservedDuration,
            reservedAt = reservedAt,
            route = route,
            taskDistance = taskDistance
        )
    }


    override suspend fun selectedOrder(rowOrder: Int): CourierOrderLocalDataEntity { //сюда приходит 0
        return courierLocalRepository.orderAndOffices(rowOrder)

    }

//    override fun selectedOrder(rowOrder: Int): Single<CourierOrderLocalDataEntity> { //сюда приходит 0
//        return courierLocalRepository.orderAndOffices(rowOrder)
//            .compose(rxSchedulerFactory.applySingleSchedulers())
//    }

    override fun mapState(state: CourierMapState) {
        // если убрать флаг на карте не будет работаь
        courierMapRepository.mapState(state)

    }

    override fun observeMapAction(): Flow<CourierMapAction> {
        return courierMapRepository.observeMapAction()


    }

    override fun carNumberIsConfirm(): Boolean {
        return userManager.carNumber().isNotEmpty()

    }

    override fun isDemoMode(): Boolean {
        return tokenManager.isDemo()

    }

    override fun carNumber(): String {
        return userManager.carNumber().replaceCarNumberY()
    }

    override fun carType(): Int {
        return userManager.carType()
    }

    var orderId: String? = null

    override suspend fun anchorTask() {
        val courierOrderLocalDataEntity = selectedOrder(selectedRowOrder())
        val courierWarehouseLocalEntity = courierLocalRepository.readCurrentWarehouse()// null here
        val localOrderEntity = convertToLocalOrderEntity(courierOrderLocalDataEntity, courierWarehouseLocalEntity)
        reserveTask(localOrderEntity)
        orderId = localOrderEntity.orderId.toString()
        courierLocalRepository.setOrderInReserve(localOrderEntity)
    }

    override suspend fun deleteOrderAfterCloseCourierOrder() {//7823
        val taskId = courierLocalRepository.getOrderId()
        appRemoteRepository.deleteTask(orderId?:taskId)
        courierLocalRepository.deleteOrder()
    }

    private suspend fun reserveTask(it: LocalOrderEntity) =
        appRemoteRepository.reserveTask(it.orderId.toString(), userManager.carNumber())




    private fun convertToLocalOrderEntity(
        orderEntity: CourierOrderLocalDataEntity,
        warehouseLocalEntity: CourierWarehouseLocalEntity
    ) = with(orderEntity.courierOrderLocalEntity) {
        LocalOrderEntity(
            orderId = id,
            routeID = routeID,
            gate = gate,
            minPrice = minPrice,
            minVolume = minVolume,
            minBoxes = minBoxesCount,
            countOffices = orderEntity.dstOffices.size,
            wbUserID = -1,
            carNumber = userManager.carNumber(),
            reservedAt = timeManager.getLocalTime(),
            startedAt = "",
            reservedDuration = reservedDuration,
            status = TaskStatus.TIMER.status,
            cost = 0,
            srcId = warehouseLocalEntity.id,
            srcName = warehouseLocalEntity.name,
            srcAddress = warehouseLocalEntity.fullAddress,
            srcLongitude = warehouseLocalEntity.longitude,
            srcLatitude = warehouseLocalEntity.latitude,
            route = route ?: "не указан"
        )
    }

}

