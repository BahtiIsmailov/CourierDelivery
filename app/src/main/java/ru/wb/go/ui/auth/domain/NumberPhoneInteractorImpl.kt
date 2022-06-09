package ru.wb.go.ui.auth.domain

import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.wb.go.network.api.auth.AuthRemoteRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory

class NumberPhoneInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val authRepository: AuthRemoteRepository,
) : NumberPhoneInteractor {

    override fun userPhone(): String {
        return authRepository.userPhone()
    }

    override suspend fun couriersExistAndSavePhone(phone: String)  {
        return withContext(Dispatchers.IO){
            authRepository.couriersExistAndSavePhone(phone)
        }
    }

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }


}