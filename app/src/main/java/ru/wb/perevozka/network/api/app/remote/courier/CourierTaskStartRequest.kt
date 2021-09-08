package ru.wb.perevozka.network.api.app.remote.courier

data class CourierTaskStartRequest(
    val id: String,
    val dstOfficeID: Int,
    val loadingAt: String,
    val deliveredAt: String,
)