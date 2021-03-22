package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class CarResponse(
    @SerializedName("id") var id: Int,
    @SerializedName("plateNumber") var plateNumber: String
)

