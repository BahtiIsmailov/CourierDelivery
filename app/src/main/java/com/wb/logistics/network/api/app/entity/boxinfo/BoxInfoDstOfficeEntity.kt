package com.wb.logistics.network.api.app.entity.boxinfo

data class BoxInfoDstOfficeEntity(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val longitude: Double,
    val latitude: Double,
)