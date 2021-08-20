package ru.wb.perevozka.ui.courierorders.domain

import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.perevozka.db.entity.courier.CourierOrderEntity
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.ui.splash.domain.AppSharedRepository

class CourierOrderInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
) : CourierOrderInteractor {

    override fun orders(srcOfficeID: Int): Single<List<CourierOrderEntity>> {
        return appRemoteRepository.courierOrders(srcOfficeID)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

}