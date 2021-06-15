package com.wb.logistics.network.api.app.entity.boxinfo

import androidx.room.Entity

data class BoxInfoSrcOfficeEntity(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val longitude: Double,
    val latitude: Double,
)