package ru.wb.go.network.api.app.entity

data class CourierTaskBoxEntity(
    val id: String,
    val dstOfficeID: Int,
    val loadingAt: String,
    val deliveredAt: String,
)