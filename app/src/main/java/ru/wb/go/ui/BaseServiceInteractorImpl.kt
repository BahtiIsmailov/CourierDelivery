package ru.wb.go.ui

import io.reactivex.Observable
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.utils.managers.DeviceManager

abstract class BaseServiceInteractorImpl(
    protected val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    protected val deviceManager: DeviceManager,
) : BaseServiceInteractor {

    override fun observeNetworkConnected(): Observable<NetworkState> {
        return networkMonitorRepository.networkConnected()
            .compose(rxSchedulerFactory.applyObservableSchedulers())
    }

    override fun versionApp(): String {
        return deviceManager.appVersion
    }

}