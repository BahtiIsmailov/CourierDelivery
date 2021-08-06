package ru.wb.perevozka.ui.unloadingforcedtermination.domain

import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import ru.wb.perevozka.network.monitor.NetworkState
import io.reactivex.Completable
import io.reactivex.Observable

interface ForcedTerminationInteractor {

    fun observeNotUnloadedBoxBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

    fun completeUnloading(currentOfficeId: Int, dataLog: String): Completable

    fun observeNetworkConnected(): Observable<NetworkState>

}