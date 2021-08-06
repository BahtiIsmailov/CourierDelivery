package ru.wb.perevozka.network.api.app.remote.pvz

data class BoxToPvzBalanceResponse(
    val srcOffice: BoxToPvzBalanceSrcOfficeResponse,
    val dstOffice: BoxToPvzBalanceDstOfficeResponse,
    val barcode: String,
    val updatedAt: String,
    val status: Int,
)