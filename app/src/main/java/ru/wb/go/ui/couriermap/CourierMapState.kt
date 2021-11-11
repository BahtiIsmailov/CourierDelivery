package ru.wb.go.ui.couriermap

import org.osmdroid.util.BoundingBox
import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapCircle
import ru.wb.go.utils.map.MapPoint

sealed class CourierMapState {

    data class UpdateMarkers(val points: List<CourierMapMarker>) : CourierMapState()

    data class NavigateToPointByZoomRadius(val startNavigation: MapCircle) : CourierMapState()

    data class ZoomToCenterBoundingBox(val boundingBox: BoundingBox) : CourierMapState()

    data class NavigateToMarker(val id: String) : CourierMapState()

    data class NavigateToPoint(val mapPoint: MapPoint) : CourierMapState()

    object NavigateToMyLocation : CourierMapState()

    // TODO: 14.10.2021 переработать на указание абстрактной точки
    data class UpdateAndNavigateToMyLocationPoint(val point: CoordinatePoint) : CourierMapState()

    object UpdateMyLocation : CourierMapState()

}