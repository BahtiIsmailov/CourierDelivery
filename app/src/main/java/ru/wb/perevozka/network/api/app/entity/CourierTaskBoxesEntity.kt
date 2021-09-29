package ru.wb.perevozka.network.api.app.entity

data class CourierTaskBoxesEntity(
    val data: List<CourierTaskBoxEntity>,
    val count: Int
)