package com.wb.logistics.db.entity.boxinfo

import androidx.room.Entity

@Entity
data class DstOfficeEntity(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
)