package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class Car(
    @SerializedName("id") val id: Int,
    @SerializedName("plateNumber") val plateNumber: String
)

