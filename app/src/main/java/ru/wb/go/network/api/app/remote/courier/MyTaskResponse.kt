package ru.wb.go.network.api.app.remote.courier

import ru.wb.go.ui.courierunloading.data.FakeBeep

data class MyTaskResponse(
    val id: Int,
    val ridMask:Long?,
    val route:String?,
    val gate: String?,
    val srcOffice: MySrcOfficeResponse,
    val minCost: String?,
    val minVolume: Int,
    val minBoxesCount: Int,
    val dstOffices: List<MyDstOfficeResponse>,
    val wbUserID: Int?,
    val carNumber: String?,
    val reservedAt: String?,
    val startedAt: String?,
    val reservedDuration: String,
    val status: String?,
    val cost: String?
)