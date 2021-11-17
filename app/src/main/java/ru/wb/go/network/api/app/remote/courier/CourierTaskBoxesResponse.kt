package ru.wb.go.network.api.app.remote.courier

data class CourierTaskBoxesResponse(
    val data: List<CourierTaskBoxResponse>,
    val count: Int
)