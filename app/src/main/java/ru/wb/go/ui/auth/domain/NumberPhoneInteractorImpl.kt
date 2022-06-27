package ru.wb.go.ui.auth.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.network.api.auth.AuthRemoteRepository
import ru.wb.go.network.monitor.NetworkMonitorRepository
import ru.wb.go.network.monitor.NetworkState

class NumberPhoneInteractorImpl(
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val authRepository: AuthRemoteRepository,
) : NumberPhoneInteractor {

    override fun userPhone(): String {
        return authRepository.userPhone()
    }

    override suspend fun couriersExistAndSavePhone(phone: String)  {
        return authRepository.couriersExistAndSavePhone(phone)

    }

    override fun observeNetworkConnected(): Flow<NetworkState> {
        return networkMonitorRepository.networkConnected()

    }


}