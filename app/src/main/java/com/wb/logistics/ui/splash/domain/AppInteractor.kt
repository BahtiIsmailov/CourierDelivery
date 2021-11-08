package com.wb.logistics.ui.splash.domain

import com.wb.logistics.ui.splash.AppVersionState
import com.wb.logistics.utils.managers.ScreenManagerImpl
import io.reactivex.Observable
import io.reactivex.Single

interface AppInteractor {

    fun isNetworkConnected(): Observable<Boolean>
    fun exitAuth()
    fun observeUpdatedStatus() : Observable<ScreenManagerImpl.NavigateComplete>
    fun observeCountBoxes() : Observable<AppDeliveryResult>
    fun checkUpdateApp(): Single<AppVersionState>
    fun getUpdateApp(destination : String): Single<AppVersionState>

}