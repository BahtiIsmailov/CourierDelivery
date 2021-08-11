package ru.wb.perevozka.ui.auth.domain

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.perevozka.network.api.auth.AuthRemoteRepository
import ru.wb.perevozka.network.api.auth.response.CheckExistPhoneResponse
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory

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

    override fun couriersExistAndSavePhone(phone: String): Completable {
        return authRepository.couriersExistAndSavePhone(phone)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }


}