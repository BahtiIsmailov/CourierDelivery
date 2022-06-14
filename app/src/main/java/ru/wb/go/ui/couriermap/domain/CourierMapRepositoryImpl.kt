package ru.wb.go.ui.couriermap.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.onEmpty
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

class CourierMapRepositoryImpl : CourierMapRepository {



    private val mapStateSubject = MutableSharedFlow<CourierMapState>()
    private val mapActionSubject = MutableSharedFlow<CourierMapAction>()

    override suspend fun observeMapState(): Flow<CourierMapState> {
        return mapStateSubject
    }

    override suspend fun mapState(state: CourierMapState) {
        mapStateSubject.emit(state)
    }

    override suspend fun mapAction(action: CourierMapAction) {
        mapActionSubject.emit(action) // закинуть в поток
    }

    override suspend fun observeMapAction(): Flow<CourierMapAction> {
        return mapActionSubject
    }

}