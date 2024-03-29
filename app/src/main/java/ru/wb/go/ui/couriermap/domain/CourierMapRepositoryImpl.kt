package ru.wb.go.ui.couriermap.domain

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

class CourierMapRepositoryImpl : CourierMapRepository {

    private var mapStateSubject = Channel<CourierMapState>(
       capacity = Channel.UNLIMITED
    )
    private var mapActionSubject = Channel<CourierMapAction>(
        capacity = Channel.UNLIMITED, onBufferOverflow = BufferOverflow.DROP_OLDEST
    )


    override fun observeMapState(): Flow<CourierMapState> {
        return mapStateSubject.receiveAsFlow()
    }

    override fun mapState(state: CourierMapState) {
        mapStateSubject.trySend(state)
    }

    override fun mapAction(action: CourierMapAction) {
        mapActionSubject.trySend(action)
    }

    override fun observeMapAction(): Flow<CourierMapAction> {
        return mapActionSubject.receiveAsFlow()
    }


}
