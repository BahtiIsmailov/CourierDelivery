package ru.wb.go.ui.couriermap.domain

import io.reactivex.Observable
import ru.wb.go.ui.couriermap.CourierMapAction
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.utils.managers.DeviceManager
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint

class CourierMapInteractorImpl(
    private val courierMapRepository: CourierMapRepository,
    private val deviceManager: DeviceManager
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

    override fun onForcedLocationUpdate(point: CoordinatePoint) {
        deviceManager.saveLocation("${point.latitude}:${point.longitude}")
        courierMapRepository.mapAction(CourierMapAction.LocationUpdate(point))
    }

}