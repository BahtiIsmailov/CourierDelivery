package com.wb.logistics.network.api.app.remote.flight

data class OfficeRemote(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)
