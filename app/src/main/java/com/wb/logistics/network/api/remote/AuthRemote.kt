package com.wb.logistics.network.api.remote

import com.google.gson.annotations.SerializedName

data class AuthRemote (
    @SerializedName("password") var password: String,
    @SerializedName("phone") var phone: String,
    @SerializedName("useSMS") var useSMS: Boolean
)