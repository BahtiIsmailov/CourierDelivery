package ru.wb.go.network.api.app.remote.pvzmatchingboxes

data class PvzMatchingSrcOfficeResponse(
    val id: Int,
    val name: String?,
    val fullAddress: String?,
    val long: Double?,
    val lat: Double?,
)