package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class FlightResponse(
    @SerializedName("id") var id: Int,
    @SerializedName("gate") var gate: Int,
    @SerializedName("dc") var dc: DcResponse,
    @SerializedName("offices") var offices: List<OfficeResponse>,
    @SerializedName("driver") var driver: DriverResponse,
    @SerializedName("route") var route: RouteResponse,
    @SerializedName("car") var car: CarResponse,
    @SerializedName("plannedDate") var plannedDate: String,
    @SerializedName("startedDate") var startedDate: String,
    @SerializedName("status") var status: String
)

