package com.wb.logistics.utils.managers

import androidx.navigation.NavDirections
import com.wb.logistics.network.api.app.FlightStatus
import io.reactivex.Completable
import io.reactivex.Single

interface ScreenManager {
    fun loadState(): Single<NavDirections>
    fun saveState(flightStatus: FlightStatus,
                  isGetFromGPS: Boolean = false): Completable
    fun saveState(flightStatus: FlightStatus,
                  officeId: Int,
                  isGetFromGPS: Boolean = false): Completable
    fun readState(): NavDirections
    fun clear()
}