package ru.wb.go.ui.couriermap

import ru.wb.go.utils.map.MapPoint

interface CourierMapMarker {
    var point: MapPoint
    var icon: Int
}

data class Empty(override var point: MapPoint, override var icon: Int) : CourierMapMarker
data class Failed(override var point: MapPoint, override var icon: Int) : CourierMapMarker
data class Complete(override var point: MapPoint, override var icon: Int) : CourierMapMarker
data class Wait(override var point: MapPoint, override var icon: Int) : CourierMapMarker