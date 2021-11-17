package ru.wb.go.ui.flightsloader.domain

import ru.wb.go.network.api.auth.entity.UserInfoEntity
import ru.wb.go.network.monitor.NetworkState
import io.reactivex.Observable
import io.reactivex.Single

interface FlightsLoaderInteractor {
    fun updateFlight(): Single<FlightDefinitionAction>
    fun sessionInfo(): Single<UserInfoEntity>
    fun observeNetworkConnected(): Observable<NetworkState>
}