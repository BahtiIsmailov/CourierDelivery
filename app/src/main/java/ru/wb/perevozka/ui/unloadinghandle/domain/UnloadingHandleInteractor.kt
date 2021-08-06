package ru.wb.perevozka.ui.unloadinghandle.domain

import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import io.reactivex.Observable

interface UnloadingHandleInteractor {

    fun observeAttachedBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

}