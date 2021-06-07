package com.wb.logistics.ui.flightloader.domain

import com.wb.logistics.db.FlightData
import com.wb.logistics.db.SuccessOrEmptyData
import com.wb.logistics.network.api.auth.entity.UserInfoEntity
import io.reactivex.Single

interface FlightsLoaderInteractor {
    fun sessionInfo(): Single<UserInfoEntity>
    fun updateFlight() : Single<SuccessOrEmptyData<FlightData>>
}