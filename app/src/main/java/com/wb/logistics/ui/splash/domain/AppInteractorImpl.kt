package com.wb.logistics.ui.splash.domain

import com.wb.logistics.network.api.auth.AuthRemoteRepository
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Observable
import io.reactivex.Single

class AppInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val repository: AuthRemoteRepository,
) : AppInteractor {
    override fun sessionInfo(): Single<Pair<String, String>> {
        return repository.userInfo().compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun isNetworkConnected(): Observable<Boolean> {
        return networkMonitorRepository.isNetworkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun exitAuth() {
        repository.clearToken()
    }

}