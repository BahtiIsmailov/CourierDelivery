package ru.wb.go.network.api.app.remote.courier

import ru.wb.go.db.entity.courierlocal.LocalBoxEntity
import ru.wb.go.ui.courierunloading.data.FakeBeep

data class ApiBoxRequest(
    val id: String,
    val dstOfficeID: Int,
    val loadingAt: String,
    val deliveredAt: String?,
    val fakeBeep: FakeBeep
)

fun LocalBoxEntity.convertToApiBoxRequest(): ApiBoxRequest {
    val deliver = when (this.deliveredAt) {
        "" -> null
        else -> this.deliveredAt
    }
    return ApiBoxRequest(this.boxId, this.officeId, this.loadingAt, deliver,FakeBeep(this.fakeOfficeId,this.fakeDeliveredAt))
}