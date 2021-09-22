package ru.wb.perevozka.db.entity.courierboxes

data class CourierIntransitGroupByOfficeEntity(
    val address: String,
    val deliveredCount: Int,
    val fromCount: Int,
    val longitude: Double,
    val latitude: Double,
    val visitedAt: String,
)