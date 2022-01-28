package ru.wb.go.ui.courierorders.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.db.CourierLocalRepository
import ru.wb.go.db.entity.courier.CourierOrderEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity
import ru.wb.go.db.entity.courierlocal.CourierOrderLocalEntity
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.UserManager
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.ui.couriermap.domain.CourierMapRepository

class CourierOrderInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val courierLocalRepository: CourierLocalRepository,
    private val courierMapRepository: CourierMapRepository,
    private val userManager: UserManager,
) : CourierOrderInteractor {

    override fun orders(srcOfficeID: Int): Single<List<CourierOrderEntity>> {
        return appRemoteRepository.courierOrders(srcOfficeID)
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

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
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

}