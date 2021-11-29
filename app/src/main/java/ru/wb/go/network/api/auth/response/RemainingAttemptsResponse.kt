package ru.wb.go.network.api.auth.response

import com.google.gson.annotations.SerializedName

data class RemainingAttemptsResponse (
    @SerializedName("remainingAttempts") var remainingAttempts: Int,
)