package ru.wb.go.ui.courierloading.domain

import ru.wb.go.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity

data class CourierBoxDefinitionResult(
    val orderDstOffice: CourierOrderDstOfficeLocalEntity?,
    val parseQrCode: ParseQrCode,
    val loadingAt: String,
    val taskId: String,
)
