package ru.wb.perevozka.network.api.app.entity

data class CourierTaskStartEntity(
    val id: String,
    val dstOfficeID: Int,
    val loadingAt: String,
)