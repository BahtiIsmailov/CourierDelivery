package com.wb.logistics.network.api.auth.response

import com.google.gson.annotations.SerializedName

data class StatisticsResponse(
    @SerializedName("smsSent") var smsSent: String,
)