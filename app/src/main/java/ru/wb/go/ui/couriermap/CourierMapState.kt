package ru.wb.go.ui.couriermap

import org.osmdroid.util.BoundingBox
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint

sealed class CourierMapState {

    data class UpdateMarkers(val points: List<CourierMapMarker>) : CourierMapState()

    data class ZoomToCenterBoundingBox(val boundingBox: BoundingBox) : CourierMapState()

    data class NavigateToMarker(val id: String) : CourierMapState()

    data class NavigateToPoint(val mapPoint: MapPoint) : CourierMapState()

    object NavigateToMyLocation : CourierMapState()

    data class UpdateMyLocationPoint(val point: CoordinatePoint) : CourierMapState()

    object UpdateMyLocation : CourierMapState()

}