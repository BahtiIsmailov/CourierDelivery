package ru.wb.go.ui.couriermap.domain

import io.reactivex.Observable
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint

interface CourierMapInteractor {

    fun subscribeMapState(): Observable<CourierMapState>

    fun markerClick(point: MapPoint)

    fun mapClick()

    fun onForcedLocationUpdate(point: CoordinatePoint)

    fun showAll()

}