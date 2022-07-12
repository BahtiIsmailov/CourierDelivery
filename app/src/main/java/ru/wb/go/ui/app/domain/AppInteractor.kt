package ru.wb.go.ui.app.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.network.monitor.NetworkState

interface AppInteractor {

    fun observeNetworkConnected(): Flow<NetworkState>
    fun exitAuth()
    fun observeNavigationApp(): Flow<String>

}