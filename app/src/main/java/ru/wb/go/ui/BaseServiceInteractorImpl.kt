package ru.wb.go.ui

import kotlinx.coroutines.flow.Flow
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.utils.managers.DeviceManager

abstract class BaseServiceInteractorImpl(
    private val networkMonitorRepository: NetworkMonitorRepository,
    protected val deviceManager: DeviceManager,
) : BaseServiceInteractor {

    override fun observeNetworkConnected(): Flow<NetworkState> {
        return networkMonitorRepository.networkConnected()

    }

    override fun versionApp(): String {
        return deviceManager.appVersion
    }

}