package ru.wb.go.network.api.app.remote.courier

data class Data(
    val displayFrom: String,
    val displayTo: String,
    val dstOffices: List<DstOffice>,
    val gate: String,
    val id: Int,
    val minBoxesCount: Int,
    val minPrice: Int,
    val minVolume: Int,
    val reservedDuration: Int,
    val ridMask: Long,
    val route: String,
    val routeID: Int,
    val taskDistance: Int
)