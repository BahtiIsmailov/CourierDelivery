package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class Route(
    @SerializedName("id") val id: Int,
    @SerializedName("changed") val changed: Boolean,
    @SerializedName("name") val name: String
)