package com.wb.logistics.network.api.app.remote.boxinfo

data class SrcOfficeRemote(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)