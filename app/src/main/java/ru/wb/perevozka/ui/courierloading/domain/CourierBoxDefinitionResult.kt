package ru.wb.perevozka.ui.courierloading.domain

import ru.wb.perevozka.db.entity.courierlocal.CourierOrderDstOfficeLocalEntity

data class CourierBoxDefinitionResult(
    val orderDstOffice: CourierOrderDstOfficeLocalEntity?,
    val parseQrCode: ParseQrCode,
    val loadingAt: String,
    val taskId: String,
)
