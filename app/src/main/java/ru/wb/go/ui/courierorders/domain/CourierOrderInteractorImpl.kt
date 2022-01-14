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

class CourierOrderInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val courierLocalRepository: CourierLocalRepository
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

}