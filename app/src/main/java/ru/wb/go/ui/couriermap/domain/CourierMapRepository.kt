package ru.wb.go.ui.couriermap.domain

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.Flow
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierMapRepository {

      fun mapAction(action: CourierMapAction)

     fun observeMapState(): Flow<CourierMapState>

    fun observeMapAction(): Flow<CourierMapAction>

      fun mapState(state: CourierMapState)

}