package ru.wb.go.ui

import io.reactivex.Observable
import ru.wb.go.network.monitor.NetworkState

interface BaseServiceInteractor {

    fun observeNetworkConnected(): Observable<NetworkState>

    fun versionApp(): String

}