package ru.wb.perevozka.ui.courierunloading.domain

import ru.wb.perevozka.db.entity.courierboxes.CourierBoxEntity

data class CourierUnloadingDefinitionResult(
    val boxesEntity: List<CourierBoxEntity>,
    val parseQrCode: ParseQrCode,
    val timeScan: String
)
