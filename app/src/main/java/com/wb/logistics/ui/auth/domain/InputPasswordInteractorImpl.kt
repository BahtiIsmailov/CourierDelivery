package com.wb.logistics.ui.auth.domain

import com.jakewharton.rxbinding3.InitialValueObservable
import com.wb.logistics.network.api.auth.AuthRemoteRepository
import com.wb.logistics.network.monitor.NetworkMonitorRepository
import com.wb.logistics.network.monitor.NetworkState
import com.wb.logistics.network.rx.RxSchedulerFactory
import io.reactivex.Completable
import io.reactivex.Observable

class InputPasswordInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val authRepository: AuthRemoteRepository,
) : InputPasswordInteractor {
    override fun authByPassword(phone: String, password: String): Completable {
        return authRepository.authByPhoneOrPassword(phone, password, false)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun remindPasswordChanges(observable: InitialValueObservable<CharSequence>): Observable<Boolean> {
        return observable.map { it.toString() }
            .distinctUntilChanged()
            .map { it.length >= LENGTH_PASSWORD_MIN }
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }


    companion object {
        private const val LENGTH_PASSWORD_MIN = 1
    }

}