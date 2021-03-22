package com.wb.logistics.network.api.auth.response

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("access_token") var accessToken: String,
    @SerializedName("expires_in") var expiresIn: Int,
    @SerializedName("refresh_token") var refreshToken: String
)