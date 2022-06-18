package ru.wb.go.ui

import kotlinx.coroutines.flow.Flow
import ru.wb.go.network.monitor.NetworkState

interface BaseServiceInteractor {

    fun observeNetworkConnected(): Flow<NetworkState>

    fun versionApp(): String

}