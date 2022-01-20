package ru.wb.go.ui.couriermap

import org.osmdroid.util.BoundingBox
import ru.wb.go.utils.map.CoordinatePoint

sealed class CourierMapState {

    data class UpdateMarkers(val points: List<CourierMapMarker>) : CourierMapState()

    data class ZoomToBoundingBox(val boundingBox: BoundingBox, val animate: Boolean) : CourierMapState()

    data class NavigateToMarker(val id: String) : CourierMapState()

    data class NavigateToPointZoom(val point: CoordinatePoint) : CourierMapState()

    data class NavigateToPoint(val point: CoordinatePoint) : CourierMapState()

    object NavigateToMyLocation : CourierMapState()

    data class UpdateMyLocationPoint(val point: CoordinatePoint) : CourierMapState()

    object UpdateMyLocation : CourierMapState()

}