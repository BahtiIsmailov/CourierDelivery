package com.wb.logistics.network.api.app.entity.warehousescan

data class WarehouseScanDstOfficeEntity(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val longitude: Double,
    val latitude: Double,
)