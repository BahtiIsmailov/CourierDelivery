package com.wb.logistics.ui.splash.domain

import com.wb.logistics.network.api.auth.AuthRepository
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Observable
import io.reactivex.Single

class NavigationInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val repository: AuthRepository,
) : NavigationInteractor {
    override fun sessionInfo(): Single<Pair<String, String>> {
        return repository.userInfo()
    }

    override fun isNetworkConnected(): Observable<Boolean> {
        return networkMonitorRepository.isNetworkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun exitAuth() {
        repository.clearToken()
    }

}