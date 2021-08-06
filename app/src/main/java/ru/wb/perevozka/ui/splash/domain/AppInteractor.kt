package ru.wb.perevozka.ui.splash.domain

import ru.wb.perevozka.network.monitor.NetworkState
import ru.wb.perevozka.utils.managers.ScreenManagerImpl
import io.reactivex.Observable

interface AppInteractor {

    fun observeNetworkConnected(): Observable<NetworkState>
    fun exitAuth()
    fun observeUpdatedStatus() : Observable<ScreenManagerImpl.NavigateComplete>
    fun observeCountBoxes() : Observable<AppDeliveryResult>

}