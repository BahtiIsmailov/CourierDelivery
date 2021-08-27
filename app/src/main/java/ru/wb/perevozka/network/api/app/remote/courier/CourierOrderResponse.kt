package ru.wb.perevozka.network.api.app.remote.courier

data class CourierOrderResponse(
    val id: Int,
    val routeID: Int?,
    val gate: String?,
    val srcOffice: CourierOrderSrcOfficeResponse?,
    val minPrice: Int,
    val minVolume: Int,
    val minBoxesCount: Int,
    val dstOffices: List<CourierOrderDstOfficeResponse>,
)