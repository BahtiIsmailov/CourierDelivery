package com.wb.logistics.network.api.app.remote.pvzmatchingboxes

data class PvzMatchingDstOfficeResponse(
    val id: Int,
    val name: String?,
    val fullAddress: String?,
    val long: Double?,
    val lat: Double?,
)