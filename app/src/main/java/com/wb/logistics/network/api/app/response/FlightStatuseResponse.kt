package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class FlightStatuseResponse(
    @SerializedName("status") var status: String,
    @SerializedName("description") var description: String
)