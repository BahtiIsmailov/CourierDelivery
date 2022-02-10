package ru.wb.go.ui.app.domain

import io.reactivex.Observable
import ru.wb.go.network.monitor.NetworkState

interface AppInteractor {

    fun observeNetworkConnected(): Observable<NetworkState>
    fun exitAuth()
    fun observeNavigationApp(): Observable<String>

}