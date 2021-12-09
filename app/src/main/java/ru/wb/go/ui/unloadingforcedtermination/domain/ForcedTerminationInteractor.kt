package ru.wb.go.ui.unloadingforcedtermination.domain

import ru.wb.go.db.entity.flighboxes.FlightBoxEntity
import ru.wb.go.network.monitor.NetworkState
import io.reactivex.Completable
import io.reactivex.Observable

interface ForcedTerminationInteractor {

    fun observeNotUnloadedBoxBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

    fun completeUnloading(currentOfficeId: Int, dataLog: String): Completable

    fun observeNetworkConnected(): Observable<NetworkState>

}