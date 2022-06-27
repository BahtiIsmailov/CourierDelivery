package ru.wb.go.ui.auth.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.network.monitor.NetworkState

interface NumberPhoneInteractor {

    fun userPhone() : String

    suspend fun couriersExistAndSavePhone(phone: String)

    fun observeNetworkConnected():  Flow<NetworkState>
}