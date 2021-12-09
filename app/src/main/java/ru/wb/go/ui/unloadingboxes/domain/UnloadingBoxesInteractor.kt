package ru.wb.go.ui.unloadingboxes.domain

import ru.wb.go.db.entity.flighboxes.FlightBoxEntity
import io.reactivex.Observable

interface UnloadingBoxesInteractor {

    fun observeUnloadedBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

}