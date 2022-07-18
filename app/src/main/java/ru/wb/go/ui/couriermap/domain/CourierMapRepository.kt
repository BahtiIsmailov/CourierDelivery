package ru.wb.go.ui.couriermap.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

interface CourierMapRepository {

    fun mapAction(action: CourierMapAction)

    fun observeMapState(): Flow<CourierMapState>

    fun observeMapAction(): Flow<CourierMapAction>

    fun mapState(state: CourierMapState)

    fun clearCacheSharedFlow()

}