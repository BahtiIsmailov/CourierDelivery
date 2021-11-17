package ru.wb.go.network.monitor

import io.reactivex.Observable

interface NetworkMonitorRepository {

    fun networkConnected(): Observable<NetworkState>

}