package ru.wb.go.network.api.app.entity.warehousescan

data class WarehouseScannedBoxEntity(
    val barcode: String,
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: WarehouseScannedBoxCurrentOfficeEntity,
)