package ru.wb.perevozka.ui.couriermap.domain

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import ru.wb.perevozka.ui.couriermap.CourierMapState

interface CourierMapRepository {

    fun mapAction(index: String)

    fun observeMapState(): Observable<CourierMapState>


    fun observeMapAction(): Observable<String>

    fun mapState(state: CourierMapState)

}