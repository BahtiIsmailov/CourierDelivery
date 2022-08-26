package ru.wb.go.network.api.app.remote.courier

data class DstOffice(
    val fullAddress: String,
    val id: Int,
    val lat: Double,
    val long: Double,
    val name: String,
    val unusualTime: Boolean,
    val wrkTime: String
)