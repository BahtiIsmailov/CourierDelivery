package ru.wb.perevozka.ui.unloadingboxes.domain

import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import io.reactivex.Observable

interface UnloadingBoxesInteractor {

    fun observeUnloadedBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

}