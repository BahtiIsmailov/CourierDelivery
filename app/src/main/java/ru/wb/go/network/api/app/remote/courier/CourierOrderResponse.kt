package ru.wb.go.network.api.app.remote.courier

data class CourierOrderResponse(
    val id: Int,
    val routeID: Int?,
    val route: String?,
    val gate: String?,
    val srcOffice: CourierOrderSrcOfficeResponse?,
    val minPrice: Int,
    val minVolume: Int,
    val minBoxesCount: Int,
    val dstOffices: List<CourierOrderDstOfficeResponse>,
    val reservedDuration: String,
)