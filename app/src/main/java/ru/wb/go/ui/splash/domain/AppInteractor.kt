package ru.wb.go.ui.splash.domain

import io.reactivex.Observable
import io.reactivex.Single
import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.ui.auth.AppVersionState
import ru.wb.go.utils.managers.ScreenManagerImpl

interface AppInteractor {

    fun observeNetworkConnected(): Observable<NetworkState>
    fun exitAuth()
    fun observeUpdatedStatus() : Observable<ScreenManagerImpl.NavigateComplete>
    fun observeCountBoxes() : Observable<AppDeliveryResult>
    fun onSearchChange(query: String)
    fun checkUpdateApp(): Single<AppVersionState>
    fun getUpdateApp(destination: String): Single<AppVersionState>

}