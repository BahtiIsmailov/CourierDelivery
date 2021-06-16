package com.wb.logistics.network.api.app.remote.warehousescan

data class WarehouseScannedBoxRemote(
    val barcode: String,
    val isManualInput: Boolean,
    val updatedAt: String,
    val currentOffice: WarehouseScannedBoxCurrentOfficeRemote,
)