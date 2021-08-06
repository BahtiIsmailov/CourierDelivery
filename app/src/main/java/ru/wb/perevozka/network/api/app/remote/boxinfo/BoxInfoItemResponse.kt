package ru.wb.perevozka.network.api.app.remote.boxinfo

data class BoxInfoItemResponse(
    val barcode: String,
    val srcOffice: BoxInfoSrcOfficeResponse,
    val dstOffice: BoxInfoDstOfficeResponse,
    val smID: Int,
)