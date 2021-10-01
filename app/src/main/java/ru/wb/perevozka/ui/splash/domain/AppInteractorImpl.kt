package ru.wb.perevozka.ui.splash.domain

import io.reactivex.Observable
import ru.wb.perevozka.db.AppLocalRepository
import ru.wb.perevozka.network.api.auth.AuthRemoteRepository
import ru.wb.perevozka.network.monitor.NetworkMonitorRepository
import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.network.rx.RxSchedulerFactory
import ru.wb.perevozka.utils.managers.ScreenManager
import ru.wb.perevozka.utils.managers.ScreenManagerImpl

class AppInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val authRemoteRepository: AuthRemoteRepository,
    private val appLocalRepository: AppLocalRepository,
    private val appSharedRepository: AppSharedRepository,
    private val screenManager: ScreenManager,
) : AppInteractor {

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun exitAuth() {
        authRemoteRepository.clearToken()
        appLocalRepository.clearAll()
    }

    override fun observeCountBoxes(): Observable<AppDeliveryResult> {
        return appLocalRepository.observeNavDrawerCountBoxes()
            .toObservable()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun onSearchChange(query: String) {
        appSharedRepository.search(query)
    }

    override fun observeUpdatedStatus(): Observable<ScreenManagerImpl.NavigateComplete> {
        return screenManager.observeUpdatedStatus()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

}

data class AppDeliveryResult(
    val acceptedCount: Int,
    val returnCount: Int,
    val deliveryCount: Int,
    val debtCount: Int,
)