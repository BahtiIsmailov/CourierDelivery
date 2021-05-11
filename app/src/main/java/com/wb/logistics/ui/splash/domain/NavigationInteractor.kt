package com.wb.logistics.ui.splash.domain

import io.reactivex.Observable
import io.reactivex.Single

interface NavigationInteractor {
    fun sessionInfo(): Single<Pair<String, String>>
    fun isNetworkConnected(): Observable<Boolean>
    fun exitAuth()
}