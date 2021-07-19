package com.wb.logistics.ui.unloadingboxes.domain

import com.wb.logistics.db.entity.flighboxes.FlightBoxEntity
import io.reactivex.Observable

interface UnloadingBoxesInteractor {

    fun observeUnloadedBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

}