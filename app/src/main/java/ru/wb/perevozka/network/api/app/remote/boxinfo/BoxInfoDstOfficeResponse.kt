package ru.wb.perevozka.network.api.app.remote.boxinfo

data class BoxInfoDstOfficeResponse(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)