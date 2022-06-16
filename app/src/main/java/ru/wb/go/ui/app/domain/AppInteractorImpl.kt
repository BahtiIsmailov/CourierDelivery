package ru.wb.go.ui.app.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import ru.wb.go.network.api.auth.AuthRemoteRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState

class AppInteractorImpl(
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val authRemoteRepository: AuthRemoteRepository,
    private val appNavRepository: AppNavRepository,
) : AppInteractor {

    override suspend fun observeNetworkConnected(): NetworkState {
        return withContext(Dispatchers.IO) {
            networkMonitorRepository.networkConnected()
        }
    }

    override suspend fun exitAuth() {
        withContext(Dispatchers.IO){
            authRemoteRepository.clearCurrentUser()
        }
    }

    override fun observeNavigationApp(): Flow<String> {
        return appNavRepository.observeNavigation()
            .flowOn(Dispatchers.IO)

    }



}