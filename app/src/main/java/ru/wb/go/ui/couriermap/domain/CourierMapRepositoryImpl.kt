package ru.wb.go.ui.couriermap.domain

import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState

class CourierMapRepositoryImpl : CourierMapRepository {

    lateinit var mapActionSubject:CourierMapAction
    //private var mapActionSubject = PublishSubject.create<CourierMapAction>()

    lateinit var mapStateSubject:CourierMapState
    //private val mapStateSubject = PublishSubject.create<CourierMapState>()

    override fun observeMapState(): CourierMapState {
        return mapStateSubject
    }

    override fun mapState(state: CourierMapState) {
        mapStateSubject = state
    }

    override fun mapAction(action: CourierMapAction) {
        mapActionSubject = action
    }

    override fun observeMapAction(): CourierMapAction {
        return mapActionSubject
    }

}