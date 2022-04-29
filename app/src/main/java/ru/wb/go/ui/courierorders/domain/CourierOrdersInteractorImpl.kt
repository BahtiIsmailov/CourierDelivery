package ru.wb.go.ui.courierorders.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
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
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.domain.CourierMapRepository
import ru.wb.go.utils.LogUtils
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.TimeManager
import ru.wb.go.utils.prefs.SharedWorker

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

    override fun freeOrdersLocalClearAndSave(srcOfficeID: Int): Single<MutableList<CourierOrderLocalDataEntity>> {
        return appTasksRepository.getFreeOrders(srcOfficeID)
            .map { freeOrders -> freeOrders.sortedBy { it.id }.toMutableList() }
            .flatMap {
                courierLocalRepository.deleteAllOrder()
                courierLocalRepository.deleteAllOrderOffices()
                val localEntity = toCourierOrderLocalDataEntities(it)
                courierLocalRepository.saveFreeOrders(localEntity)
                    .andThen(Single.just(localEntity))
            }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun freeOrdersLocal(): Single<MutableList<CourierOrderLocalDataEntity>> {
        return courierLocalRepository.freeOrders()
            .map { it.toMutableList() }
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    private fun toCourierOrderLocalDataEntities(it: MutableList<CourierOrderEntity>): MutableList<CourierOrderLocalDataEntity> {
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


//    override fun clearAndSaveSelectedOrder(
//        courierOrderEntity: CourierOrderEntity,
//        rowOrder: Int
//    ): Completable {
//        courierLocalRepository.deleteAllOrder()
//        courierLocalRepository.deleteAllOrderOffices()
//        val courierOrderLocalEntity = convertCourierOrderLocalEntity(courierOrderEntity) //rowOrder
//        val courierOrderDstOfficesLocalEntity =
//            convertCourierOrderDstOfficesLocalEntity(
//                courierOrderEntity.dstOffices,
//                courierOrderEntity.id
//            )
//        return courierLocalRepository.saveOrderAndOffices(
//            courierOrderLocalEntity, courierOrderDstOfficesLocalEntity
//        ).compose(rxSchedulerFactory.applyCompletableSchedulers())
//    }

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
                        visitedAt = ""
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
        )
    }


    override fun selectedOrder(rowOrder: Int): Single<CourierOrderLocalDataEntity> {
        return courierLocalRepository.orderAndOffices(rowOrder)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun mapState(state: CourierMapState) {
        courierMapRepository.mapState(state)
    }

    override fun observeMapAction(): Observable<CourierMapAction> {
        return courierMapRepository.observeMapAction()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun carNumberIsConfirm(): Boolean {
        return userManager.carNumber().isNotEmpty()
    }

    override fun isDemoMode(): Boolean {
        return tokenManager.isDemo()
    }

    override fun carNumber(): String {
        return userManager.carNumber()
    }

//    return appRemoteRepository.reserveTask(
//    orderEntity.id.toString(),
//    userManager.carNumber()
//    )

    override fun anchorTask(): Completable {
        return Completable.fromSingle(Single.zip(
            selectedOrder(selectedRowOrder()),
            courierLocalRepository.readCurrentWarehouse()
        ) { orderEntity: CourierOrderLocalDataEntity, warehouseLocalEntity: CourierWarehouseLocalEntity ->
            val reservedTime = timeManager.getLocalTime()
            with(orderEntity.courierOrderLocalEntity) {
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
                    reservedAt = reservedTime,
                    startedAt = "",
                    reservedDuration = reservedDuration,
                    status = TaskStatus.TIMER.status,
                    cost = 0,
                    srcId = warehouseLocalEntity.id,
                    srcName = warehouseLocalEntity.name,
                    srcAddress = warehouseLocalEntity.fullAddress,
                    srcLongitude = warehouseLocalEntity.longitude,
                    srcLatitude = warehouseLocalEntity.latitude,
                )
            }
        }
            .doOnSuccess { LogUtils { logDebugApp("anchorTask " + it) } }
            .doOnError { LogUtils { logDebugApp("anchorTask " + it) } }
            .doOnSuccess { lo -> courierLocalRepository.setOrderInReserve(lo) })
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }
}