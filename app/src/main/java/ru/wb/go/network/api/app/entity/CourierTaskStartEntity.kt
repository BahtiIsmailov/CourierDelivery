package ru.wb.go.network.api.app.entity

data class CourierTaskStartEntity(
    val id: String,
    val dstOfficeID: Int,
    val loadingAt: String,
)