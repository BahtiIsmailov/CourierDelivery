package ru.wb.go.ui.couriermap

import org.osmdroid.util.BoundingBox
import ru.wb.go.utils.map.CoordinatePoint

sealed class CourierMapState {

    data class UpdateMarkers(val points: MutableList<CourierMapMarker>) : CourierMapState()

    data class UpdateMarkersWithIndex(val points: List<CourierMapMarker>) : CourierMapState()

    data class HideMarker(val point: CourierMapMarker) : CourierMapState()

    data class UpdateMarkersWithAnimateToPosition(
        val pointsHide: List<CourierMapMarker>,
        val pointFrom: CoordinatePoint,
        val pointsTo: List<CourierMapMarker>
    ) : CourierMapState()

    data class ZoomToBoundingBox(val boundingBox: BoundingBox, val animate: Boolean) :
        CourierMapState()

    data class ZoomToBoundingBoxOffsetY(
        val boundingBox: BoundingBox,
        val animate: Boolean,
        val offsetY: Int
    ) : CourierMapState()

    data class NavigateToMarker(val id: String) : CourierMapState()

    data class NavigateToPointZoom(val point: CoordinatePoint) : CourierMapState()

    data class NavigateToPoint(val point: CoordinatePoint) : CourierMapState()

    object NavigateToMyLocation : CourierMapState()

    data class UpdateMyLocationPoint(val point: CoordinatePoint) : CourierMapState()

    object UpdateMyLocation : CourierMapState()

}