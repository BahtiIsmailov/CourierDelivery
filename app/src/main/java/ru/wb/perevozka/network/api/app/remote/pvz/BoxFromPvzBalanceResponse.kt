package ru.wb.perevozka.network.api.app.remote.pvz

data class BoxFromPvzBalanceResponse(
    val srcOffice: BoxFromPvzBalanceSrcOfficeResponse,
    val dstOffice: BoxFromPvzBalanceDstOfficeResponse,
    val barcode: String,
    val updatedAt: String,
    val status: Int,
)