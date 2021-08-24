package ru.wb.perevozka.db.entity.courier

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CourierOrderSrcOfficeEntity(
    val id: Int,
    val name: String,
    val fullAddress: String,
    val long: Double,
    val lat: Double,
) : Parcelable