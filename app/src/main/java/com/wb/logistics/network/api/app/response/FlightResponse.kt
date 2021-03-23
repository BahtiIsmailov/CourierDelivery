package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class FlightResponse(
    @SerializedName("flight") val flight: Flight?,
    @SerializedName("boxes") val boxes: List<Box>?
)
