package ru.wb.perevozka.ui.courierloading.domain

import ru.wb.perevozka.db.entity.courierlocal.CourierOrderLocalDataEntity

data class CourierLoadingDefinitionResult(
    val courierOrderLocalDataEntity: CourierOrderLocalDataEntity,
    val parseQrCode: ParseQrCode,
    val timeScan: String
)
