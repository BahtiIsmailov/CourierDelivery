package ru.wb.go.utils.map

import org.osmdroid.util.BoundingBox
import kotlin.math.*

class MapEnclosingCircle {

    fun minimumBoundingBoxRelativelyMyLocation(
        points: List<CoordinatePoint>,
        myLocation: CoordinatePoint,
        radiusKm: Int
    ): BoundingBox {
        var maxLatPoint = myLocation.latitude
        var maxLongPoint = myLocation.longitude
        var minLatPoint = myLocation.latitude
        var minLongPoint = myLocation.longitude

        if (points.size == 1) {
            val firstPoint = points[0]
            maxLatPoint = max(myLocation.latitude, firstPoint.latitude)
            maxLongPoint = max(myLocation.longitude, firstPoint.longitude)
            minLatPoint = min(myLocation.latitude, firstPoint.latitude)
            minLongPoint = min(myLocation.longitude, firstPoint.longitude)
        } else if (points.size > 1) {
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

    fun allCoordinatePointToBoundingBox(points: List<CoordinatePoint>): BoundingBox {
        var maxLat = Double.MAX_VALUE
        var maxLong = Double.MAX_VALUE
        var minLat = Double.MIN_VALUE
        var minLong = Double.MIN_VALUE
        for (i in points.indices) {
            val point = points[i]
            val lat: Double = point.latitude
            val lon: Double = point.longitude
            if (i == 0) {
                maxLat = lat + OFFSET_SINGLE_POINT
                maxLong = lon + OFFSET_SINGLE_POINT
                minLat = lat - OFFSET_SINGLE_POINT
                minLong = lon - OFFSET_SINGLE_POINT
            }
            if (lat > maxLat) maxLat = lat
            if (lon > maxLong) maxLong = lon
            if (lat < minLat) minLat = lat
            if (lon < minLong) minLong = lon
        }
        return BoundingBox(maxLat, maxLong, minLat, minLong)
    }

    private fun circleFrom(pointA: CoordinatePoint, pointB: CoordinatePoint): MapCircle {
        val pointCenter = center(pointA, pointB)
        val radius = distance(pointA, pointB) / 2.0
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
        return sqrt(
            (pointA.latitude - pointB.latitude).pow(2.0) + (pointA.longitude - pointB.longitude).pow(
                2.0
            )
        )
    }

    private fun isInside(mapCircle: MapCircle, point: CoordinatePoint): Boolean {
        return distance(mapCircle.point, point) >= mapCircle.radius
    }

    private fun distanceKm(pointA: CoordinatePoint, pointB: CoordinatePoint): Double {
        val latARad = Math.toRadians(pointA.latitude)
        val latBRad = Math.toRadians(pointB.latitude)
        val deltaLonRad = Math.toRadians(pointB.longitude - pointA.longitude)
        return acos(
            sin(latARad) * sin(latBRad) + cos(latARad) * cos(latBRad) * cos(deltaLonRad)
        ) * EARTH_RADIUS_KM
    }

    companion object {
        const val EARTH_RADIUS_KM = 6371
        const val OFFSET_SINGLE_POINT = 0.01
    }

}