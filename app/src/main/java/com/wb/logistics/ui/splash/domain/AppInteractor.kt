package com.wb.logistics.ui.splash.domain

import io.reactivex.Observable

interface AppInteractor {
    fun isNetworkConnected(): Observable<Boolean>
    fun exitAuth()
}