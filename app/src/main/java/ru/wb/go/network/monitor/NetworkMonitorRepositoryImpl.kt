package ru.wb.go.network.monitor

import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow

class NetworkMonitorRepositoryImpl : NetworkMonitorRepository {

    override fun networkConnected(): Flow<NetworkState> {
        return NetworkState.connect
    }

}