package ru.wb.go.network.api.app.remote.courier

data class MyTaskResponse(
    val id: Int,
    val routeID: Int?,
    val route:String?,
    val gate: String?,
    val srcOffice: MySrcOfficeResponse,
    val minPrice: Int,
    val minVolume: Int,
    val minBoxesCount: Int,
    val dstOffices: List<MyDstOfficeResponse>,
    val wbUserID: Int,
    val carNumber: String,
    val reservedAt: String,
    val startedAt: String?,
    val reservedDuration: String,
    val status: String?,
    val cost: Int?,
)