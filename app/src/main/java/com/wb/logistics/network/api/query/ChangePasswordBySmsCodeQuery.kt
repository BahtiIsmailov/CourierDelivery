package com.wb.logistics.network.api.query

import com.google.gson.annotations.SerializedName

data class ChangePasswordBySmsCodeQuery (
    @SerializedName("password") var password: String,
    @SerializedName("tmpPassword") var tmpPassword: String,
)