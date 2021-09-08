package ru.wb.perevozka.network.api.app.remote.courier

data class CourierTasksMy(
    val id: Int,
    val routeID: Int?,
    val gate: String?,
    val minPrice: Int,
    val minVolume: Int,
    val minBoxesCount: Int,
    val dstOffices: List<CourierTaskMyDstOfficeResponse>,
    val status: String,
    val reservedAt: String,
    val reservedDuration: String,
    val startedAt: String,
)