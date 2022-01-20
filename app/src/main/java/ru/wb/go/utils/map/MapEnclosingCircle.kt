package ru.wb.go.utils.map

import org.osmdroid.util.BoundingBox
import ru.wb.go.utils.LogUtils
import kotlin.math.*

class MapEnclosingCircle {

    fun minimumBoundingBoxRelativelyMyLocation(
        points: List<CoordinatePoint>,
        myLocation: CoordinatePoint,
        radiusKm: Int
    ): BoundingBox {

        LogUtils { logDebugApp("myLocation " + myLocation) }

        val firstPoint = points[0]
        var maxLatPoint = max(myLocation.latitude, firstPoint.latitude)
        var maxLongPoint = max(myLocation.longitude, firstPoint.longitude)
        var minLatPoint = min(myLocation.latitude, firstPoint.latitude)
        var minLongPoint = min(myLocation.longitude, firstPoint.longitude)

        if (points.size > 1) {
            val pointDistances = mutableListOf<Pair<CoordinatePoint, Double>>()
            points.forEach { pointDistances.add(Pair(it, distanceKm(it, myLocation))) }
            pointDistances.filter { it.second <= radiusKm }
                .ifEmpty {
                    listOf(
                        pointDistances.filter { it.second > radiusKm }.minByOrNull { it.second }!!
                    )
                }
                .forEach {
                    with(it.first) {
                        maxLatPoint = max(maxLatPoint, latitude)
                        maxLongPoint = max(maxLongPoint, longitude)
                        minLatPoint = min(minLatPoint, latitude)
                        minLongPoint = min(minLongPoint, longitude)
                    }
                }
        }
        return BoundingBox(maxLatPoint, maxLongPoint, minLatPoint, minLongPoint)
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

    private fun isValidCircle(mapCircle: MapCircle, points: List<CoordinatePoint>): Boolean {
        points.forEach {
            if (!isInside(mapCircle, it)) return false
        }
        return true
    }

    fun distance(pointA: CoordinatePoint, pointB: CoordinatePoint): Double {
        return kotlin.math.sqrt(
            (pointA.latitude - pointB.latitude).pow(2.0) + (pointA.longitude - pointB.longitude).pow(
                2.0
            )
        )
    }

    private fun isInside(mapCircle: MapCircle, point: CoordinatePoint): Boolean {
        return distance(mapCircle.point, point) >= mapCircle.radius
    }

    fun distanceKm(pointA: CoordinatePoint, pointB: CoordinatePoint): Double {
        val latARad = Math.toRadians(pointA.latitude)
        val latBRad = Math.toRadians(pointB.latitude)
        val deltaLonRad = Math.toRadians(pointB.longitude - pointA.longitude)
        return acos(
            sin(latARad) * sin(latBRad) + cos(latARad) * cos(latBRad) * cos(deltaLonRad)
        ) * EARTH_RADIUS_KM
    }

    companion object {
        const val EARTH_RADIUS_KM = 6371
    }

}