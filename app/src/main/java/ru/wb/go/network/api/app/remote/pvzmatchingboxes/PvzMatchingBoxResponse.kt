package ru.wb.go.network.api.app.remote.pvzmatchingboxes

data class PvzMatchingBoxResponse(
    val barcode: String,
    val srcOffice: PvzMatchingSrcOfficeResponse,
    val dstOffice: PvzMatchingDstOfficeResponse,
    val smID: Int,
)