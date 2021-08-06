package ru.wb.perevozka.ui.unloadingreturnboxes.domain

import ru.wb.perevozka.db.entity.flighboxes.FlightBoxEntity
import io.reactivex.Completable
import io.reactivex.Observable

interface UnloadingReturnInteractor {

    fun removeReturnBoxes(currentOfficeId: Int, checkedBoxes: List<String>): Completable

    fun observeReturnBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

}