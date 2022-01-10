package ru.wb.go.utils.map

import org.osmdroid.util.BoundingBox
import ru.wb.go.utils.LogUtils
import kotlin.math.abs
import kotlin.math.pow

class MapEnclosingCircle {

    fun minimumBoundingBoxRelativelyMyLocation(
        points: List<CoordinatePoint>,
        myLocation: CoordinatePoint,
    ): BoundingBox {

        LogUtils { logDebugApp("myLocation " + myLocation) }

        var maxLatPoint = Double.MAX_VALUE
        var maxLongPoint = Double.MAX_VALUE
        var minLatPoint = Double.MIN_VALUE
        var minLongPoint = Double.MIN_VALUE
        for (i in points.indices) {
            val point = points[i]
            val lat: Double = point.latitude
            val lon: Double = point.longitude
            if (i == 0) {
                maxLatPoint = myLocation.latitude
                maxLongPoint = myLocation.longitude
                minLatPoint = myLocation.latitude
                minLongPoint = myLocation.longitude
            }

            if (lat > maxLatPoint) maxLatPoint = lat
            if (lon > maxLongPoint) maxLongPoint = lon
            if (lat < minLatPoint) minLatPoint = lat
            if (lon < minLongPoint) minLongPoint = lon
        }

        with(myLocation) {
            val maxLat = latitude + abs(latitude - maxLatPoint)
            val maxLong = longitude + abs(longitude - maxLongPoint)
            val minLat = latitude - abs(latitude - minLatPoint)
            val minLong = longitude - abs(longitude - minLongPoint)
            return BoundingBox(maxLat, maxLong, minLat, minLong)
        }
    }

    fun minimumBoundingBox(points: List<CoordinatePoint>): BoundingBox {
        var maxLat = Double.MAX_VALUE
        var maxLong = Double.MAX_VALUE
        var minLat = Double.MIN_VALUE
        var minLong = Double.MIN_VALUE
        for (i in points.indices) {
            val point = points[i]
            val lat: Double = point.latitude
            val lon: Double = point.longitude
            if (i == 0) {
                maxLat = lat
                maxLong = lon
                minLat = lat
                minLong = lon
            }
            if (lat > maxLat) maxLat = lat
            if (lon > maxLong) maxLong = lon
            if (lat < minLat) minLat = lat
            if (lon < minLong) minLong = lon
        }
        return BoundingBox(maxLat, maxLong, minLat, minLong)
    }

    fun minimumEnclosingCircle(points: List<CoordinatePoint>): MapCircle {
        val n = points.size
        var memCircle = MapCircle(CoordinatePoint(0.0, 0.0), 0.0)
        if (points.isEmpty()) {
            memCircle = MapCircle(CoordinatePoint(0.0, 0.0), 0.0)
        } else if (n == 1) {
            memCircle = MapCircle(CoordinatePoint(points[0].latitude, points[0].longitude), 1.0)
        } else if (n == 2) {
            val point1 = CoordinatePoint(points[0].latitude, points[0].longitude)
            val point2 = CoordinatePoint(points[1].latitude, points[1].longitude)
            memCircle = circleFrom(point1, point2)
            LogUtils { logDebugApp("memCircle " + memCircle.toString()) }
        } else {
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
        }
        return memCircle
    }

    private fun circleFrom(pointA: CoordinatePoint, pointB: CoordinatePoint): MapCircle {
        val pointCenter = center(pointA, pointB)
        val radius = distance(pointA, pointB) / 2.0
        LogUtils { logDebugApp("circleFrom radius " + radius.toString()) }
        return MapCircle(pointCenter, radius)
    }

    private fun center(pointA: CoordinatePoint, pointB: CoordinatePoint): CoordinatePoint {
        return CoordinatePoint(
            (pointA.latitude + pointB.latitude) / 2.0,
            (pointA.longitude + pointB.longitude) / 2.0
        )
    }

    private fun distance(pointA: CoordinatePoint, pointB: CoordinatePoint): Double {
        return kotlin.math.sqrt(
            (pointA.latitude - pointB.latitude).pow(2.0) + (pointA.longitude - pointB.longitude).pow(
                2.0
            )
        )
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