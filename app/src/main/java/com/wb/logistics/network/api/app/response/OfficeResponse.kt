package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class OfficeResponse(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String,
    @SerializedName("phone") var fullAddress: Int,
    @SerializedName("long") var long: List<Double>,
    @SerializedName("lat") var lat: Double
)
