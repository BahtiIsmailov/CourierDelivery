package ru.wb.perevozka.ui.courierintransit

import ru.wb.perevozka.utils.map.MapCircle
import ru.wb.perevozka.utils.map.MapPoint

sealed class CourierIntransitMapPoint {

    data class InitMapPoint(
        val pointsState: List<CourierIntransitMapPointItem>,
        val startNavigation: MapCircle
    ) :
        CourierIntransitMapPoint()

    data class UpdateMapPoints(
        val pointsState: List<CourierIntransitMapPointItem>
    ) : CourierIntransitMapPoint()

    data class NavigateToPointById(val id: String) : CourierIntransitMapPoint()

    data class NavigateToPoint(val mapPoint: MapPoint) : CourierIntransitMapPoint()

}