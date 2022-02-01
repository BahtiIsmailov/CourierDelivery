package ru.wb.go.ui.couriermap.domain

import io.reactivex.Observable
import ru.wb.go.network.rx.RxSchedulerFactory
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint

class CourierMapInteractorImpl(
    private val rxSchedulerFactory: RxSchedulerFactory,
    private val courierMapRepository: CourierMapRepository,
) : CourierMapInteractor {

    override fun subscribeMapState(): Observable<CourierMapState> {
        return courierMapRepository.observeMapState()
    }

    override fun onItemClick(point: MapPoint) {
        courierMapRepository.mapAction(CourierMapAction.ItemClick(point))
    }

    override fun onMapClick() {
        courierMapRepository.mapAction(CourierMapAction.MapClick)
    }

    override fun onInitPermission() {
        courierMapRepository.mapAction(CourierMapAction.PermissionComplete)
    }

    override fun onDeniedPermission(point: CoordinatePoint) {
        courierMapRepository.mapAction(CourierMapAction.PermissionDenied(point))
    }

    override fun onForcedLocationUpdate(point: CoordinatePoint) {
        courierMapRepository.mapAction(CourierMapAction.ForcedLocationUpdate(point))
    }

}