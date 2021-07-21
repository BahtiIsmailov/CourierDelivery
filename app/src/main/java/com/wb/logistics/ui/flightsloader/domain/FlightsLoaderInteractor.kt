package com.wb.logistics.ui.flightsloader.domain

import com.wb.logistics.network.api.auth.entity.UserInfoEntity
import io.reactivex.Single

interface FlightsLoaderInteractor {
    fun updateFlight(): Single<FlightDefinitionAction>
    fun sessionInfo(): Single<UserInfoEntity>
}