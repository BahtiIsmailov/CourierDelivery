package com.wb.logistics.ui.unloadingforcedtermination.domain

import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import com.wb.logistics.network.monitor.NetworkState
import io.reactivex.Completable
import io.reactivex.Observable

interface ForcedTerminationInteractor {

    fun observeNotUnloadedBoxBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

    fun completeUnloading(currentOfficeId: Int, dataLog: String): Completable

    fun observeNetworkConnected(): Observable<NetworkState>

}