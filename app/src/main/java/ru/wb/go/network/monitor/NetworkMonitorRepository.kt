package ru.wb.go.network.monitor

import kotlinx.coroutines.flow.Flow

interface NetworkMonitorRepository {

    fun networkConnected(): Flow<NetworkState>

}