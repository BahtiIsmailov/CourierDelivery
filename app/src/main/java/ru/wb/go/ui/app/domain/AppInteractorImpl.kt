package ru.wb.go.ui.app.domain

import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.wb.go.network.api.auth.AuthRemoteRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory

class AppInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val authRemoteRepository: AuthRemoteRepository,
    private val appNavRepository: AppNavRepository,
) : AppInteractor {

    override suspend fun observeNetworkConnected():  NetworkState {
        return withContext(Dispatchers.IO){
            networkMonitorRepository.networkConnected()
        }
    }

    override fun exitAuth() {
        authRemoteRepository.clearCurrentUser()
    }

    override fun observeNavigationApp(): Observable<String> {
        return appNavRepository.observeNavigation()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

}