package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class Office(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val fullAddress: Int,
    @SerializedName("long") val long: Double,
    @SerializedName("lat") val lat: Double
)
