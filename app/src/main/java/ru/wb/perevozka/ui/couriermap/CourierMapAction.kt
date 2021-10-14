package ru.wb.perevozka.ui.couriermap

import ru.wb.perevozka.utils.map.CoordinatePoint

sealed class CourierMapAction {

    data class ItemClick(val index: String) : CourierMapAction()

    object PermissionComplete : CourierMapAction()

    data class AutomatedLocationUpdate(val point: CoordinatePoint) : CourierMapAction()

    data class ForcedLocationUpdate(val point: CoordinatePoint) : CourierMapAction()

}