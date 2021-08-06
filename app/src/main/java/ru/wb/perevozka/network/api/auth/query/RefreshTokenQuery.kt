package ru.wb.perevozka.network.api.auth.query

import com.google.gson.annotations.SerializedName

data class RefreshTokenQuery (
    @SerializedName("refreshToken") var refreshToken: String
)