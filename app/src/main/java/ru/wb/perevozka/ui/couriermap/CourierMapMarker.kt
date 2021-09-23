package ru.wb.perevozka.ui.couriermap

import ru.wb.perevozka.utils.map.MapPoint

interface CourierMapMarker {
    var point: MapPoint
    var icon: Int
}

data class Empty(override var point: MapPoint, override var icon: Int) : CourierMapMarker
data class Failed(override var point: MapPoint, override var icon: Int) : CourierMapMarker
data class Complete(override var point: MapPoint, override var icon: Int) : CourierMapMarker