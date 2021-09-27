package ru.wb.perevozka.utils.map

import ru.wb.perevozka.utils.LogUtils
import kotlin.math.pow

class MapEnclosingCircle {

    fun minimumEnclosingCircle(points: List<CoordinatePoint>): MapCircle {
        val n = points.size
        var memCircle = MapCircle(CoordinatePoint(0.0, 0.0), 0.0)
        if (points.isEmpty()) MapCircle(CoordinatePoint(0.0, 0.0), 0.0)
        if (n == 1) MapCircle(CoordinatePoint(points[0].x, points[0].y), 0.0)
        for (i in 0 until n) {
            for (j in i + 1 until n) {
                val tmpCircle = circleFrom(points[i], points[j])
                LogUtils { logDebugApp("tmpCircle " + tmpCircle.toString()) }
                if (tmpCircle.radius > memCircle.radius) {
                    if (n == 3 || isValidCircle(tmpCircle, points))
                        memCircle = tmpCircle
                }
            }
        }
        return memCircle
    }

    private fun circleFrom(pointA: CoordinatePoint, pointB: CoordinatePoint): MapCircle {
        val pointCenter = center(pointA, pointB)
        val radius = distance(pointA, pointB) / 2.0
        return MapCircle(pointCenter, radius)
    }

    private fun center(pointA: CoordinatePoint, pointB: CoordinatePoint): CoordinatePoint {
        return CoordinatePoint((pointA.x + pointB.x) / 2.0, (pointA.y + pointB.y) / 2.0)
    }

    private fun distance(pointA: CoordinatePoint, pointB: CoordinatePoint): Double {
        return kotlin.math.sqrt((pointA.x - pointB.x).pow(2.0) + (pointA.y - pointB.y).pow(2.0))
    }

    private fun isValidCircle(mapCircle: MapCircle, points: List<CoordinatePoint>): Boolean {
        points.forEach {
            if (!isInside(mapCircle, it)) return false
        }
        return true
    }

    private fun isInside(mapCircle: MapCircle, point: CoordinatePoint): Boolean {
        return distance(mapCircle.point, point) >= mapCircle.radius
    }

}