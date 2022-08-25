package ru.wb.go.utils.map

enum class PointType{
    WAREHOUSE, CLUSTER, ORDER, ORDER_ITEM
}

data class MapPoint(val id: String, val lat: Double, val long: Double,val type: PointType?)