package ru.wb.go.db.entity.courier

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CourierOrderEntity(
    val id: Int,
    val routeID: Int,
    val route:String,
    val gate: String,
    val ridMask:Long?,
    val minCost: String,
    val minVolume: Int,
    val minBoxesCount: Int,
    var dstOffices: List<CourierOrderDstOfficeEntity>,
    val reservedDuration: String,
    val reservedAt: String,
    val taskDistance:String
) : Parcelable
