package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class Driver(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String,
    @SerializedName("phone") val fullAddress: String
)
