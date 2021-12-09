package ru.wb.go.network.monitor

import io.reactivex.Observable

class NetworkMonitorRepositoryImpl : NetworkMonitorRepository {

    override fun networkConnected(): Observable<NetworkState> {
        return NetworkState.connect
    }

}