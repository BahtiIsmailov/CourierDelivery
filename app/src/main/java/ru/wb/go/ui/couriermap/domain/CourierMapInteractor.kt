package ru.wb.go.ui.couriermap.domain

import io.reactivex.Observable
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.utils.map.CoordinatePoint

interface CourierMapInteractor {

    fun subscribeMapState(): Observable<CourierMapState>

    fun onItemClick(index: String)

    fun onInitPermission()

    fun onForcedLocationUpdate(point: CoordinatePoint)

}