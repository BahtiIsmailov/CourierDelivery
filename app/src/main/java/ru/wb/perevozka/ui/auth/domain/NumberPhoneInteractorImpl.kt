package ru.wb.perevozka.ui.auth.domain

import ru.wb.perevozka.network.api.auth.AuthRemoteRepository
import ru.wb.perevozka.network.api.auth.response.CheckExistPhoneResponse
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory
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