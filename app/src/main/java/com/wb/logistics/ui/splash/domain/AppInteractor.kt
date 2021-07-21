package com.wb.logistics.ui.splash.domain

import com.wb.logistics.utils.managers.ScreenManagerImpl
import io.reactivex.Observable

interface AppInteractor {

    fun isNetworkConnected(): Observable<Boolean>
    fun exitAuth()
    fun observeUpdatedStatus() : Observable<ScreenManagerImpl.NavigateComplete>
    fun observeCountBoxes() : Observable<AppDeliveryResult>

}