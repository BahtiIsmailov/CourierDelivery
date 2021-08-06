package ru.wb.perevozka.network.api.app.entity.warehousescan

data class WarehouseScanSrcOfficeEntity(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val longitude: Double,
    val latitude: Double,
)