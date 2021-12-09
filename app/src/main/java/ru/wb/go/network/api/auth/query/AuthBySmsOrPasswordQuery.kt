package ru.wb.go.network.api.auth.query

import com.google.gson.annotations.SerializedName

data class AuthBySmsOrPasswordQuery (
    @SerializedName("password") var password: String,
    @SerializedName("phone") var phone: String,
    @SerializedName("useSMS") var useSMS: Boolean,
)
