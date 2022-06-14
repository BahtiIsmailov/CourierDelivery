package ru.wb.go.ui.couriermap.domain

import io.reactivex.Observable
import kotlinx.coroutines.flow.Flow
import ru.wb.go.ui.couriermap.CourierMapState
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint

interface CourierMapInteractor {

    suspend fun subscribeMapState(): Flow<CourierMapState>

    suspend fun markerClick(point: MapPoint)

    suspend fun mapClick()

    suspend fun onForcedLocationUpdate(point: CoordinatePoint)

    suspend fun showAll()

    suspend fun animateComplete()

    suspend fun prolongTimeHideManager()

    suspend fun startVisibilityManagerTimer1()

}