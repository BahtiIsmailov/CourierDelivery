package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class DcResponse(
    @SerializedName("id") var id: Int,
    @SerializedName("name") var name: String,
    @SerializedName("fullAddress") var fullAddress: String,
    @SerializedName("long") var long: List<Double>,
    @SerializedName("lat") var lat: Double
)

