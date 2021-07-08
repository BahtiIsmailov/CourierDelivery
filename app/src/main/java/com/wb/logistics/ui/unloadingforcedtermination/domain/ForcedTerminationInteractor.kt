package com.wb.logistics.ui.unloadingforcedtermination.domain

import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import io.reactivex.Completable
import io.reactivex.Observable

interface ForcedTerminationInteractor {

    fun observeNotUnloadedBoxBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

    fun completeUnloading(currentOfficeId: Int, dataLog: String): Completable

}