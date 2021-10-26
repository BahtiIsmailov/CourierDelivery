package ru.wb.perevozka.utils.map

import org.osmdroid.util.BoundingBox
import ru.wb.perevozka.app.AppConsts
import ru.wb.perevozka.utils.LogUtils
import kotlin.math.abs
import kotlin.math.pow

class MapEnclosingCircle {

    fun minimumBoundingBoxRelativelyMyLocation(
        points: List<CoordinatePoint>,
        myLocation: CoordinatePoint,
        latOffset: Double,
        longOffset: Double
    ): BoundingBox {

        LogUtils { logDebugApp("myLocation " + myLocation) }

        var maxLat = myLocation.latitude + latOffset
        var maxLong = myLocation.longitude + longOffset
        var minLat = myLocation.latitude - latOffset
        var minLong = myLocation.longitude - longOffset

        val searchLocation = CoordinatePoint(
            myLocation.latitude + latOffset,
            myLocation.longitude + longOffset
        )

        var nearPoint = myLocation

        val searchDistance = distance(myLocation, searchLocation)
        var minDistance = searchDistance
        var distance: Double
        for (i in points.indices) {
            distance = distance(myLocation, points[i])
            if (distance < searchDistance) {
                return BoundingBox(maxLat, maxLong, minLat, minLong)
            }
            LogUtils { logDebugApp("nearPoint 1 " + nearPoint) }
            if (i == 0 || distance < minDistance) {
                LogUtils { logDebugApp("minDistance " + minDistance + " distance " + distance) }
                minDistance = distance
                LogUtils { logDebugApp("i == 0 || minDistance if (distance < minDistance) " + minDistance + " distance " + distance) }
                nearPoint = points[i]
            }

        }

        LogUtils { logDebugApp("nearPoint 2 " + nearPoint) }

        val offsetLatitude = abs(nearPoint.latitude - myLocation.latitude)
        val offsetLongitude = abs(nearPoint.longitude - myLocation.longitude)

        LogUtils { logDebugApp("offsetLatitude " + offsetLatitude + " offsetLongitude " + offsetLongitude) }

        maxLat = myLocation.latitude + offsetLatitude
        maxLong = myLocation.longitude + offsetLongitude
        minLat = myLocation.latitude - offsetLatitude
        minLong = myLocation.longitude - offsetLongitude
        return BoundingBox(maxLat, maxLong, minLat, minLong)
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