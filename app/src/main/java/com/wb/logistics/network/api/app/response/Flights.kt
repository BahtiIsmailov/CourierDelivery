package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class Flights(
    @SerializedName("data") val data: List<Flight>
)