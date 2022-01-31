package ru.wb.go.ui.splash.domain

import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.auth.AppVersionState

interface AppInteractor {

    fun observeNetworkConnected(): Observable<NetworkState>
    fun exitAuth()
    fun onSearchChange(query: String)
}