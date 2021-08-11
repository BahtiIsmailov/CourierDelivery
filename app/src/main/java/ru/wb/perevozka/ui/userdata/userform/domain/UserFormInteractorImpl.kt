package ru.wb.perevozka.ui.userdata.userform.domain

import io.reactivex.Completable
import io.reactivex.Observable
import ru.wb.perevozka.network.api.auth.AuthRemoteRepository
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory

class UserFormInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val authRemoteRepository: AuthRemoteRepository,
) : UserFormInteractor {

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun couriersForm(phone: String): Completable {
        return authRemoteRepository.couriersForm(phone)
            .compose(rxSchedulerFactory.applyCompletableSchedulers())
    }

}