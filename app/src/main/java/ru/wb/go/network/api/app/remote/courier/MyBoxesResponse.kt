package ru.wb.go.network.api.app.remote.courier

import ru.wb.go.ui.courierunloading.data.FakeBeepResponse

data class MyBoxesResponse(
    val id: String,
    val dstOfficeID: Int,
    val loadingAt: String,
    val deliveredAt: String?,
    val fakeBeep: FakeBeepResponse?
)