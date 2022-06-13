package ru.wb.go.ui

import io.reactivex.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.utils.managers.DeviceManager

abstract class BaseServiceInteractorImpl(
    protected val rxSchedulerFactory: RxSchedulerFactory,
    private val networkMonitorRepository: NetworkMonitorRepository,
    protected val deviceManager: DeviceManager,
) : BaseServiceInteractor {

    override suspend fun observeNetworkConnected(): NetworkState  {
        return withContext(Dispatchers.IO){
            networkMonitorRepository.networkConnected()
        }
    }

    override fun versionApp(): String {
        return deviceManager.appVersion
    }

}