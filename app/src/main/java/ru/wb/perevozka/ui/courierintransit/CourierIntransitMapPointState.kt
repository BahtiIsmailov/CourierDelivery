package ru.wb.perevozka.ui.courierintransit

import ru.wb.perevozka.utils.map.MapPoint

interface CourierIntransitMapPointItem {
    var point: MapPoint
    var icon: Int
}

data class Empty(override var point: MapPoint, override var icon: Int) : CourierIntransitMapPointItem
data class Failed(override var point: MapPoint, override var icon: Int) : CourierIntransitMapPointItem
data class Complete(override var point: MapPoint, override var icon: Int) : CourierIntransitMapPointItem