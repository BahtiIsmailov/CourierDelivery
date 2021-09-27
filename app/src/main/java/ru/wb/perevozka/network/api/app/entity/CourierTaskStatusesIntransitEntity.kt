package ru.wb.perevozka.network.api.app.entity

data class CourierTaskStatusesIntransitEntity(
    val id: String,
    val dstOfficeID: Int,
    val loadingAt: String,
    val deliveredAt: String?,
)