package ru.wb.go.network.api.auth.query

import com.google.gson.annotations.SerializedName

data class PasswordCheckQuery (
    @SerializedName("tmpPassword") var tmpPassword: String
)