package ru.wb.go.ui.settings.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState

class SettingsInteractorImpl(
    private val networkMonitorRepository: NetworkMonitorRepository,
) : SettingsInteractor {
    override  fun observeNetworkConnected(): Flow<NetworkState> {
        return networkMonitorRepository.networkConnected()
    }
}

