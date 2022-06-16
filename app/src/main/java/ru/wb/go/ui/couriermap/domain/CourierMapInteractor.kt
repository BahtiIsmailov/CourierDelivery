package ru.wb.go.ui.couriermap.domain

import kotlinx.coroutines.flow.Flow
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint

interface CourierMapInteractor {

    fun subscribeMapState(): Flow<CourierMapState>

      fun markerClick(point: MapPoint)

      fun mapClick()

      fun onForcedLocationUpdate(point: CoordinatePoint)

      fun showAll()

      fun animateComplete()

    suspend fun prolongTimeHideManager()

    fun startVisibilityManagerTimer1()

}