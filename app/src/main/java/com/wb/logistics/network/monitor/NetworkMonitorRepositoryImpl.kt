package com.wb.logistics.network.monitor

import io.reactivex.Observable

class NetworkMonitorRepositoryImpl : NetworkMonitorRepository {

    override fun isNetworkConnected(): Observable<Boolean> {
        return NetworkState.connect
    }

}