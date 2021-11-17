package ru.wb.go.ui.unloadingreturnboxes.domain

import ru.wb.go.db.entity.flighboxes.FlightBoxEntity
import io.reactivex.Completable
import io.reactivex.Observable

interface UnloadingReturnInteractor {

    fun removeReturnBoxes(currentOfficeId: Int, checkedBoxes: List<String>): Completable

    fun observeReturnBoxes(currentOfficeId: Int): Observable<List<FlightBoxEntity>>

}