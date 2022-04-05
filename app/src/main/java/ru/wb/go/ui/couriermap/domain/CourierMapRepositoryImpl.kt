package ru.wb.go.ui.couriermap.domain

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

class CourierMapRepositoryImpl : CourierMapRepository {

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

}