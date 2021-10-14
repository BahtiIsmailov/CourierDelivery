package ru.wb.perevozka.ui.couriermap

import ru.wb.perevozka.utils.map.CoordinatePoint
import ru.wb.perevozka.utils.map.MapCircle
import ru.wb.perevozka.utils.map.MapPoint

sealed class CourierMapState {

    data class UpdateMarkers(val pointsState: List<CourierMapMarker>) : CourierMapState()

    data class NavigateToPointByZoomRadius(val startNavigation: MapCircle) : CourierMapState()

    data class NavigateToMarker(val id: String) : CourierMapState()

    data class NavigateToPoint(val mapPoint: MapPoint) : CourierMapState()

    object NavigateToMyLocation : CourierMapState()

    // TODO: 14.10.2021 переработать на указание абсттрактной точки
    data class UpdateAndNavigateToMyLocationPoint(val point: CoordinatePoint) : CourierMapState()

    object UpdateMyLocation : CourierMapState()

}