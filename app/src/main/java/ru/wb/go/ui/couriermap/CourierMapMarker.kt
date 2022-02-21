package ru.wb.go.ui.couriermap

import ru.wb.go.ui.courierintransit.IntransitItemType
import ru.wb.go.utils.map.MapPoint

interface CourierMapMarker {
    var point: MapPoint
    var icon: Int
}

interface IntransitMapMarker : CourierMapMarker {
    var type: IntransitItemType
}

data class Empty(override var point: MapPoint, override var icon: Int) : CourierMapMarker

data class Intransit(
    override var point: MapPoint,
    override var icon: Int,
    override var type: IntransitItemType
) : IntransitMapMarker