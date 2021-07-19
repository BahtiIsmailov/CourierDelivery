package com.wb.logistics.ui.unloadinghandle.domain

import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import io.reactivex.Observable

interface UnloadingHandleInteractor {

    fun observeAttachedBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

}