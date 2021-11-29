package ru.wb.go.ui.couriermap.domain

import io.reactivex.Observable
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.utils.map.CoordinatePoint

class CourierMapInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val courierMapRepository: CourierMapRepository,
) : CourierMapInteractor {

    override fun subscribeMapState(): Observable<CourierMapState> {
        return courierMapRepository.observeMapState()
    }

    override fun onItemClick(index: String) {
        courierMapRepository.mapAction(CourierMapAction.ItemClick(index))
    }

    override fun onInitPermission() {
        courierMapRepository.mapAction(CourierMapAction.PermissionComplete)
    }

    override fun onForcedLocationUpdate(point: CoordinatePoint) {
        courierMapRepository.mapAction(CourierMapAction.ForcedLocationUpdate(point))
    }

}