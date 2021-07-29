package com.wb.logistics.ui.splash.domain

import com.wb.logistics.network.monitor.NetworkState
import com.wb.logistics.utils.managers.ScreenManagerImpl
import io.reactivex.Observable

interface AppInteractor {

    fun observeNetworkConnected(): Observable<NetworkState>
    fun exitAuth()
    fun observeUpdatedStatus() : Observable<ScreenManagerImpl.NavigateComplete>
    fun observeCountBoxes() : Observable<AppDeliveryResult>

}