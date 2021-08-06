package ru.wb.perevozka.network.monitor

import io.reactivex.Observable

interface NetworkMonitorRepository {

    fun networkConnected(): Observable<NetworkState>

}