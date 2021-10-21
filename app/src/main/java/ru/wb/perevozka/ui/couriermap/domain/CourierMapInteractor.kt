package ru.wb.perevozka.ui.couriermap.domain

import io.reactivex.Observable
import ru.wb.perevozka.ui.couriermap.CourierMapState
import ru.wb.perevozka.utils.map.CoordinatePoint

interface CourierMapInteractor {

    fun subscribeMapState(): Observable<CourierMapState>

    fun onItemClick(index: String)

    fun onInitPermission()

    fun onForcedLocationUpdate(point: CoordinatePoint)

}