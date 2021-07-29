package com.wb.logistics.ui.auth.domain

import com.wb.logistics.network.api.auth.AuthRemoteRepository
import com.wb.logistics.network.api.auth.response.CheckExistPhoneResponse
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.monitor.NetworkState
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Observable
import io.reactivex.Single

class NumberPhoneInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val authRepository: AuthRemoteRepository,
) : NumberPhoneInteractor {

    override fun userPhone(): String {
        return authRepository.userPhone()
    }

    override fun checkExistAndSavePhone(phone: String): Single<CheckExistPhoneResponse> {
        return authRepository.checkExistAndSavePhone(phone)
            .compose(rxSchedulerFactory.applySingleSchedulers())
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }


}