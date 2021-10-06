package ru.wb.perevozka.ui.couriermap.domain

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import ru.wb.perevozka.ui.couriermap.CourierMapAction
import ru.wb.perevozka.ui.couriermap.CourierMapState

interface CourierMapRepository {

    fun mapAction(action: CourierMapAction)

    fun observeMapState(): Observable<CourierMapState>


    fun observeMapAction(): Observable<CourierMapAction>

    fun mapState(state: CourierMapState)

}