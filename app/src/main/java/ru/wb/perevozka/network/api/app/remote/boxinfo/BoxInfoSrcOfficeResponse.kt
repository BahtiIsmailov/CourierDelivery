package ru.wb.perevozka.network.api.app.remote.boxinfo

data class BoxInfoSrcOfficeResponse(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)