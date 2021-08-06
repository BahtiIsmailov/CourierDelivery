package ru.wb.perevozka.network.api.app.entity.boxinfo

data class BoxInfoSrcOfficeEntity(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val longitude: Double,
    val latitude: Double,
)