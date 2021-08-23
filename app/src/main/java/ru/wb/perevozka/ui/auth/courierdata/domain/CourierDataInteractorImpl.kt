package ru.wb.perevozka.ui.auth.courierdata.domain

import io.reactivex.Completable
import io.reactivex.Observable
import ru.wb.perevozka.network.api.app.AppRemoteRepository
import ru.wb.perevozka.network.api.app.entity.CourierDocumentsEntity
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory

class CourierDataInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
) : CourierDataInteractor {

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun courierDocuments(courierDocumentsEntity: CourierDocumentsEntity): Completable {
        return appRemoteRepository.courierDocuments(courierDocumentsEntity)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

}