package com.wb.logistics.network.api.app.response

import com.google.gson.annotations.SerializedName

data class RouteResponse(
    @SerializedName("id") var id: Int,
    @SerializedName("changed") var changed: Boolean,
    @SerializedName("name") var name: String
)
