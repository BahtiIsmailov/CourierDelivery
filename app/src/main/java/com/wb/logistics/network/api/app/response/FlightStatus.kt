package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class FlightStatus(
    @SerializedName("status") val status: String,
    @SerializedName("description") val description: String
)