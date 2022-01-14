package ru.wb.go.network.api.app.remote.courier

data class MySrcOfficeResponse(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)