package ru.wb.go.ui.settings.domain

import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory

class SettingsInteractorImpl(
    private val networkMonitorRepository: NetworkMonitorRepository,
) : SettingsInteractor {
    override  fun observeNetworkConnected(): NetworkState {
        return networkMonitorRepository.networkConnected()

    }
}

/*
 override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

 */