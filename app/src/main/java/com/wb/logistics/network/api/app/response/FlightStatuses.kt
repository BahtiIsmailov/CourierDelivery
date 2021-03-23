package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName


data class FlightStatuses(
    @SerializedName("data") val data: List<FlightStatus>
)