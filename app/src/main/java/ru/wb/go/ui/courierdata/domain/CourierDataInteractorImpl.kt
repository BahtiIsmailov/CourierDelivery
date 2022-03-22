package ru.wb.go.ui.courierdata.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.network.api.app.AppRemoteRepository
import ru.wb.go.network.api.app.entity.CourierDocumentsEntity
import ru.wb.go.network.exceptions.InternalServerException
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.network.token.UserManager

class CourierDataInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRemoteRepository: AppRemoteRepository,
    private val userManager: UserManager,
) : CourierDataInteractor {

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun saveCourierDocuments(courierDocumentsEntity: CourierDocumentsEntity): Completable {
        return appRemoteRepository.saveCourierDocuments(courierDocumentsEntity)
            .doOnError {
                if (it is InternalServerException) {
                    userManager.saveCourierDocumentsEntity(courierDocumentsEntity)
                }
            }
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun getCourierDocuments(): Single<CourierDocumentsEntity> {
        return appRemoteRepository.getCourierDocuments()
            .compose(rxSchedulerFactory.applySingleSchedulers())


    }

}