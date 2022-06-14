package ru.wb.go.ui.app.domain

import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import ru.wb.go.network.monitor.NetworkState

interface AppInteractor {

    suspend fun observeNetworkConnected(): NetworkState
    fun exitAuth()
    suspend fun observeNavigationApp(): Flow<String>

}