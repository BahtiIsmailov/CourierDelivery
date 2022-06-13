package ru.wb.go.network.monitor

import io.reactivex.Observable

class NetworkMonitorRepositoryImpl : NetworkMonitorRepository {

    override fun networkConnected():  NetworkState {
        return NetworkState.connect
    }

}