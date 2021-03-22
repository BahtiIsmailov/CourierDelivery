package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class FlightsResponse(
    @SerializedName("data") var data: List<FlightResponse>
)