package ru.wb.go.ui.couriermap.domain

import io.reactivex.Observable
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint

interface CourierMapInteractor {

    fun subscribeMapState(): Observable<CourierMapState>

    fun onItemClick(point: MapPoint)

    fun onMapClick()

    fun onInitPermission()

    fun onDeniedPermission(point: CoordinatePoint)

    fun onForcedLocationUpdate(point: CoordinatePoint)

}