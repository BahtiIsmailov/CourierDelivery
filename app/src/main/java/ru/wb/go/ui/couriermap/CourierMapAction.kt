package ru.wb.go.ui.couriermap

import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint

sealed class CourierMapAction {

    data class ItemClick(val point: MapPoint) : CourierMapAction()

    object MapClick : CourierMapAction()

    data class LocationUpdate(val point: CoordinatePoint) : CourierMapAction()

    object ShowAll : CourierMapAction()

}