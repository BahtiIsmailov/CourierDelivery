package ru.wb.go.network.api.app.remote.boxinfo

data class BoxInfoSrcOfficeResponse(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)