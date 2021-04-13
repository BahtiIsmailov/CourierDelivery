package com.wb.logistics.network.api.app.remote.matchingboxes

data class MatchingSrcOfficeRemote(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)