package ru.wb.perevozka.ui.courierintransit

import ru.wb.perevozka.utils.map.MapCircle
import ru.wb.perevozka.utils.map.MapPoint

sealed class CourierIntransitMapPoint {

    data class InitMapPoint(val points: List<MapPoint>, val startNavigation: MapCircle) :
        CourierIntransitMapPoint()

    data class NavigateToPoint(val id: String, val isSelected: Boolean) : CourierIntransitMapPoint()

}