package ru.wb.go.ui.couriermap.domain

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

class CourierMapRepositoryImpl : CourierMapRepository {


    private val mapStateSubject = MutableSharedFlow<CourierMapState>(
        extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    private val mapActionSubject = MutableSharedFlow<CourierMapAction>(
        extraBufferCapacity = Int.MAX_VALUE, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    override fun observeMapState(): Flow<CourierMapState> {
        return mapStateSubject
    }

    override fun mapState(state: CourierMapState) {
        mapStateSubject.tryEmit(state)
    }

    override fun mapAction(action: CourierMapAction) {
        mapActionSubject.tryEmit(action) // закинуть в поток
    }

    override fun observeMapAction(): Flow<CourierMapAction> {
        return mapActionSubject
    }

}
