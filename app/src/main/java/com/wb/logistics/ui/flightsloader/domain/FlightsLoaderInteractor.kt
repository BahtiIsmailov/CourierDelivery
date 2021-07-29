package com.wb.logistics.ui.flightsloader.domain

import com.wb.logistics.network.api.auth.entity.UserInfoEntity
import com.wb.logistics.network.monitor.NetworkState
import io.reactivex.Observable
import io.reactivex.Single

interface FlightsLoaderInteractor {
    fun updateFlight(): Single<FlightDefinitionAction>
    fun sessionInfo(): Single<UserInfoEntity>
    fun observeNetworkConnected(): Observable<NetworkState>
}