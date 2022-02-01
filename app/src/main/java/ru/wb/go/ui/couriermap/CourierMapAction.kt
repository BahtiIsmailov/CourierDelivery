package ru.wb.go.ui.couriermap

import ru.wb.go.utils.map.CoordinatePoint
import ru.wb.go.utils.map.MapPoint

sealed class CourierMapAction {

    data class ItemClick(val point: MapPoint) : CourierMapAction()

    object MapClick : CourierMapAction()

    object PermissionComplete : CourierMapAction()

    data class PermissionDenied(val point: CoordinatePoint) : CourierMapAction()

    data class AutomatedLocationUpdate(val point: CoordinatePoint) : CourierMapAction()

    data class ForcedLocationUpdate(val point: CoordinatePoint) : CourierMapAction()

}