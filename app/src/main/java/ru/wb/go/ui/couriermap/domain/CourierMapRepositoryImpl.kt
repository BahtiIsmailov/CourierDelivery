package ru.wb.go.ui.couriermap.domain

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

class CourierMapRepositoryImpl : CourierMapRepository {


    private var mapStateSubject = Channel<CourierMapState>(
       Int.MAX_VALUE
    )
    private var mapActionSubject = Channel<CourierMapAction>(
        Int.MAX_VALUE
    )


    override fun observeMapState(): Flow<CourierMapState> {
        return mapStateSubject.receiveAsFlow()
    }

    override fun mapState(state: CourierMapState) {
        mapStateSubject.trySend(state)
    }

    override fun mapAction(action: CourierMapAction) {
        mapActionSubject.trySend(action) // закинуть в поток
    }

    override fun observeMapAction(): Flow<CourierMapAction> {
        return mapActionSubject.receiveAsFlow()
    }


}
