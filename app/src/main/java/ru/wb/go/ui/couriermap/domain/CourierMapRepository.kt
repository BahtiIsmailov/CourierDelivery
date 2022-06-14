package ru.wb.go.ui.couriermap.domain

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.flow.Flow
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierMapRepository {

    suspend fun mapAction(action: CourierMapAction)

    suspend fun observeMapState(): Flow<CourierMapState>

    suspend fun observeMapAction(): Flow<CourierMapAction>

    suspend fun mapState(state: CourierMapState)

}