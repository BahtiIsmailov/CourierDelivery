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
/*
private var mapActionSubject = PublishSubject.create<CourierMapAction>()

    private val mapStateSubject = PublishSubject.create<CourierMapState>()

    override fun mapAction(action: CourierMapAction) {
        mapActionSubject.onNext(action)
    }

    override fun observeMapState(): Observable<CourierMapState> {
        return mapStateSubject
    }

    override fun mapState(state: CourierMapState) {
        mapStateSubject.onNext(state)
    }

    override fun observeMapAction(): Observable<CourierMapAction> {
        return mapActionSubject
    }

 */