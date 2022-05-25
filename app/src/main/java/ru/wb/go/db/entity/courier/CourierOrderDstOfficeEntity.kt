package ru.wb.go.db.entity.courier

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CourierOrderDstOfficeEntity(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
    val workTimes: String,
    val isUnusualTime: Boolean
) : Parcelable