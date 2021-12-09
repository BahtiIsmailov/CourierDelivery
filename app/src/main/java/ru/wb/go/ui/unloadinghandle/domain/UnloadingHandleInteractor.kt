package ru.wb.go.ui.unloadinghandle.domain

import ru.wb.go.db.entity.flighboxes.FlightBoxEntity
import io.reactivex.Observable

interface UnloadingHandleInteractor {

    fun observeAttachedBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

}