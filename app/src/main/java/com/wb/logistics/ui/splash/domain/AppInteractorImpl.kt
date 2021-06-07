package com.wb.logistics.ui.splash.domain

import com.wb.logistics.db.AppLocalRepository
import com.wb.logistics.network.api.auth.AuthRemoteRepository
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Observable

class AppInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val authRemoteRepository: AuthRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
) : AppInteractor {

    override fun isNetworkConnected(): Observable<Boolean> {
        return networkMonitorRepository.isNetworkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun exitAuth() {
        authRemoteRepository.clearToken()
        appLocalRepository.deleteAll()
    }

}