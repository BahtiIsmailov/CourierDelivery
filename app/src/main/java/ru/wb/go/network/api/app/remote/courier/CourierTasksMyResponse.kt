package ru.wb.go.network.api.app.remote.courier

data class CourierTasksMyResponse(
    val id: Int,
    val routeID: Int?,
    val gate: String?,
    val srcOffice: CourierTaskMySrcOfficeResponse,
    val minPrice: Int,
    val minVolume: Int,
    val minBoxesCount: Int,
    val dstOffices: List<CourierTaskMyDstOfficeResponse>,
    val wbUserID: Int,
    val carNumber: String,
    val reservedAt: String,
    val startedAt: String?,
    val reservedDuration: String,
    val status: String?,
    val cost: Int?,
)