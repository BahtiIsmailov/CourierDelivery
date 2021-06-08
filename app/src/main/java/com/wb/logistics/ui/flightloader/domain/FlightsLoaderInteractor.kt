package com.wb.logistics.ui.flightloader.domain

import androidx.navigation.NavDirections
import com.wb.logistics.network.api.auth.entity.UserInfoEntity
import io.reactivex.Single

interface FlightsLoaderInteractor {
    fun navigateTo(): Single<NavDirections>
    fun sessionInfo(): Single<UserInfoEntity>
}