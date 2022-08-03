package ru.wb.go.db.entity.courierlocal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CourierTimerEntity(
    val route:String?,
    val name: String,
    val orderId: Int,
    val price: String?,
    val boxesCount: Int,
    val volume: Int,
    val countPvz: Int,
    val gate: String,
    val reservedDuration: String,
    val reservedAt: String
):Parcelable