package com.wb.logistics.network.monitor

import io.reactivex.Observable

interface NetworkMonitorRepository {

    fun isNetworkConnected(): Observable<Boolean>

}