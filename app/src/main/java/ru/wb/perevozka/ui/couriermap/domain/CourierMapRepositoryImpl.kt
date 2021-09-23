package ru.wb.perevozka.ui.couriermap.domain

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject
import ru.wb.perevozka.ui.couriermap.CourierMapState

class CourierMapRepositoryImpl : CourierMapRepository {

    private var mapActionSubject = PublishSubject.create<String>()

    private val mapStateSubject = PublishSubject.create<CourierMapState>()

    override fun mapAction(index: String) {
        mapActionSubject.onNext(index)
    }

    override fun observeMapState(): Observable<CourierMapState> {
        return mapStateSubject
    }

    override fun mapState(state: CourierMapState) {
        mapStateSubject.onNext(state)
    }

    override fun observeMapAction(): Observable<String> {
        return mapActionSubject
    }

}