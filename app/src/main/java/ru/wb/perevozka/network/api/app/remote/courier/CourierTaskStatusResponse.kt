package ru.wb.perevozka.network.api.app.remote.courier

data class CourierTaskStatusResponse(
    val status: Int,
    val description: String,
)