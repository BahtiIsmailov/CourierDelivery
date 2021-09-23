package ru.wb.perevozka.ui.couriermap

import ru.wb.perevozka.utils.map.MapCircle

sealed class CourierMapState {

    data class ZoomAllMarkers(val startNavigation: MapCircle) : CourierMapState()

    data class UpdateMapMarkers(val pointsState: List<CourierMapMarker>) : CourierMapState()

    data class NavigateToMarker(val id: String) : CourierMapState()

}