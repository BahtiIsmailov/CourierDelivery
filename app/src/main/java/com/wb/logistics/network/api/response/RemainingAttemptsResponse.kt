package com.wb.logistics.network.api.response

import com.google.gson.annotations.SerializedName

data class RemainingAttemptsResponse (
    @SerializedName("remainingAttempts") var remainingAttempts: Int,
)