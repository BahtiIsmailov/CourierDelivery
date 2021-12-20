package ru.wb.go.ui.couriermap.domain

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierMapRepository {

    fun mapAction(action: CourierMapAction)

    fun observeMapState(): Observable<CourierMapState>


    fun observeMapAction(): Observable<CourierMapAction>

    fun mapState(state: CourierMapState)

}