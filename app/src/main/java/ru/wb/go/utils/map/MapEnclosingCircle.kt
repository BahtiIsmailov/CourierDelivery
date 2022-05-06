package ru.wb.go.utils.map

import org.osmdroid.util.BoundingBox
import org.osmdroid.util.TileSystemWebMercator
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
        var maxLat = TileSystemWebMercator.MaxLatitude
        var maxLong = TileSystemWebMercator.MaxLongitude
        var minLat = TileSystemWebMercator.MinLatitude
        var minLong = TileSystemWebMercator.MinLongitude

        if (points.size == 1) {
            with(points[0]) {
                maxLat = latitude + OFFSET_SINGLE_POINT
                maxLong = longitude + OFFSET_SINGLE_POINT
                minLat = latitude - OFFSET_SINGLE_POINT
                minLong = longitude - OFFSET_SINGLE_POINT
            }
        } else {
            for (i in points.indices) {
                with(points[i]) {
                    if (i == 0) {
                        maxLat = latitude
                        maxLong = longitude
                        minLat = latitude
                        minLong = longitude
                    }
                    if (latitude > maxLat) maxLat = latitude
                    if (longitude > maxLong) maxLong = longitude
                    if (latitude < minLat) minLat = latitude
                    if (longitude < minLong) minLong = longitude
                }
            }
        }
        return BoundingBox(maxLat, maxLong, minLat, minLong)
    }

    fun distance(pointA: CoordinatePoint, pointB: CoordinatePoint): Double {
        return sqrt(
            (pointA.latitude - pointB.latitude).pow(2.0) + (pointA.longitude - pointB.longitude).pow(
                2.0
            )
        )
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