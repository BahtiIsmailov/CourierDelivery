package ru.wb.go.ui.courierunloading.domain

import ru.wb.go.db.entity.courierboxes.CourierBoxEntity

data class CourierUnloadingDefinitionResult(
    val boxesEntity: List<CourierBoxEntity>,
    val parseQrCode: ParseQrCode,
    val timeScan: String
)
