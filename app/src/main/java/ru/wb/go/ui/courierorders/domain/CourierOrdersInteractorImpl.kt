package ru.wb.go.ui.courierorders.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.TaskStatus
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
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
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.managers.TimeManager

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
    private val timeManager: TimeManager
) : BaseServiceInteractorImpl(rxSchedulerFactory, networkMonitorRepository, deviceManager),
    CourierOrdersInteractor {

    override fun getFreeOrders(srcOfficeID: Int): Single<List<CourierOrderEntity>> {
        return appTasksRepository.getFreeOrders(srcOfficeID)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun clearAndSaveSelectedOrder(courierOrderEntity: CourierOrderEntity): Completable {
        courierLocalRepository.deleteAllOrder()
        courierLocalRepository.deleteAllOrderOffices()

        val courierOrderLocalEntity = with(courierOrderEntity) {
            CourierOrderLocalEntity(
                id = id,
                routeID = routeID,
                gate = gate,
                minPrice = minPrice,
                minVolume = minVolume,
                minBoxesCount = minBoxesCount,
                reservedDuration = reservedDuration,
                reservedAt = reservedAt,
                route = route
            )
        }
        val courierOrderDstOfficesLocalEntity = mutableListOf<CourierOrderDstOfficeLocalEntity>()
        courierOrderEntity.dstOffices.forEach {
            with(it) {
                courierOrderDstOfficesLocalEntity.add(
                    CourierOrderDstOfficeLocalEntity(
                        id = id,
                        orderId = courierOrderEntity.id,
                        name = name,
                        fullAddress = fullAddress,
                        longitude = long,
                        latitude = lat,
                        // TODO: 22.09.2021 вынести в отдельную таблицу
                        visitedAt = ""
                    )
                )
            }
        }
        return courierLocalRepository.saveOrderAndOffices(
            courierOrderLocalEntity,
            courierOrderDstOfficesLocalEntity
        ).compose(rxSchedulerFactory.applyCompletableSchedulers())
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

    override fun anchorTask(orderEntity: CourierOrderEntity): Completable {

        val reservedTime = timeManager.getLocalTime()

        return appRemoteRepository.reserveTask(
            orderEntity.id.toString(),
            userManager.carNumber()
        )
            .doOnComplete {
                val wh = courierLocalRepository.readCurrentWarehouse().blockingGet()
                val ro =
                    with(orderEntity) {
                        LocalOrderEntity(
                            orderId = id,
                            routeID = routeID,
                            gate = gate,
                            minPrice = minPrice,
                            minVolume = minVolume,
                            minBoxes = minBoxesCount,
                            countOffices = dstOffices.size,
                            wbUserID = -1,
                            carNumber = userManager.carNumber(),
                            reservedAt = reservedTime,
                            startedAt = "",
                            reservedDuration = reservedDuration,
                            status = TaskStatus.TIMER.status,
                            cost = 0,
                            srcId = wh.id,
                            srcName = wh.name,
                            srcAddress = wh.fullAddress,
                            srcLongitude = wh.longitude,
                            srcLatitude = wh.latitude,
                        )
                    }
                courierLocalRepository.setOrderInReserve(ro)
            }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

}