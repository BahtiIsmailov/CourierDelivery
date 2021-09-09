package ru.wb.perevozka.utils.managers

import androidx.navigation.NavDirections
import ru.wb.perevozka.network.api.app.FlightStatus
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single

interface ScreenManager {

    fun observeUpdatedStatus() : Observable<ScreenManagerImpl.NavigateComplete>
    fun navDirection(flightId: String): Single<NavDirections>
    fun saveState(flightStatus: FlightStatus,
                  isGetFromGPS: Boolean = false): Completable
    fun saveState(flightStatus: FlightStatus,
                  officeId: Int,
                  isGetFromGPS: Boolean = false): Completable
    fun readState(): NavDirections
    fun clear()

}