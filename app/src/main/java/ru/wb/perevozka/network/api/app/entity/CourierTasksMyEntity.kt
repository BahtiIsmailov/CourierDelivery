package ru.wb.perevozka.network.api.app.entity

data class CourierTasksMyEntity(
    val id: Int,
    val routeID: Int,
    val gate: String,
//    val srcOffice: CourierTasksMySrcOfficeEntity,
    val minPrice: Int,
    val minVolume: Int,
    val minBoxesCount: Int,
    val dstOffices: List<CourierTaskMyDstOfficeEntity>,
    val wbUserID: Int,
    val carNumber: String,
    val reservedAt: String,
    val startedAt: String,
    val reservedDuration: String,
    val status: String,
)