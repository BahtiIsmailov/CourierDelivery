package ru.wb.go.ui.splash.domain

import ru.wb.go.network.monitor.NetworkState
import ru.wb.go.utils.managers.ScreenManagerImpl
import io.reactivex.Observable

interface AppInteractor {

    fun observeNetworkConnected(): Observable<NetworkState>
    fun exitAuth()
    fun observeUpdatedStatus() : Observable<ScreenManagerImpl.NavigateComplete>
    fun observeCountBoxes() : Observable<AppDeliveryResult>
    fun onSearchChange(query: String)

}