package com.wb.logistics.network.api.app.remote.warehousescan

data class WarehouseBalanceBoxSrcOfficeResponse(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)