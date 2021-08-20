package ru.wb.perevozka.db.entity.courier

data class CourierOrderEntity(
    val id: Int,
    val routeID: Int,
    val gate: String,
    val srcOffice: CourierOrderSrcOfficeEntity,
    val minPrice: Int,
    val minVolume: Int,
    val minBoxesCount: Int,
    val dstOffices: List<CourierOrderDstOfficeEntity>,
)
