package ru.wb.go.ui.app.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.network.api.auth.AuthRemoteRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState

class AppInteractorImpl(
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val authRemoteRepository: AuthRemoteRepository,
    private val appNavRepository: AppNavRepository,
) : AppInteractor {

    override fun observeNetworkConnected(): Flow<NetworkState> {
        return networkMonitorRepository.networkConnected()

    }

    override fun exitAuth() {
        authRemoteRepository.clearCurrentUser()
    }

    override fun observeNavigationApp(): Flow<String> {
        return appNavRepository.observeNavigation()
    }



}