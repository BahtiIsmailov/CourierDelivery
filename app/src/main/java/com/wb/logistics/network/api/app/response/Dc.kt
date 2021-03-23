package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class Dc(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("fullAddress") val fullAddress: String,
    @SerializedName("long") val long: List<Double>,
    @SerializedName("lat") val lat: Double
)

