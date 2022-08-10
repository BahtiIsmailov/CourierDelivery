package ru.wb.go.ui.courierunloading.data

import ru.wb.go.network.api.app.remote.courier.MySrcOfficeResponse

data class FakeBeepResponse(
    val office: MySrcOfficeResponse,
    val dt:String
)