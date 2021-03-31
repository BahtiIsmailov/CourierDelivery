package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class Flight(
    @SerializedName("id") val id: Int,
    @SerializedName("gate") val gate: Int,
    @SerializedName("dc") val dc: Dc,
    @SerializedName("offices") val offices: List<Office>,
    @SerializedName("driver") val driver: Driver,
    @SerializedName("route") val route: Route? = null,
    @SerializedName("car") val car: Car,
    @SerializedName("plannedDate") val plannedDate: String,
    @SerializedName("startedDate") val startedDate: String,
    @SerializedName("status") val status: String
)

