package ru.wb.perevozka.network.api.app.remote.courier

data class CourierTaskBoxesResponse(
    val data: List<CourierTaskBoxResponse>,
    val count: Int
)