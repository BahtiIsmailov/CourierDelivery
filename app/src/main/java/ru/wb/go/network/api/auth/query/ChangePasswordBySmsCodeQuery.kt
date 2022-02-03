package ru.wb.go.network.api.auth.query

import com.google.gson.annotations.SerializedName

data class ChangePasswordBySmsCodeQuery (
    @SerializedName("password") var password: String,
    @SerializedName("tmpPassword") var tmpPassword: String,
)