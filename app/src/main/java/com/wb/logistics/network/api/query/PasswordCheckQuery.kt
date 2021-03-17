package com.wb.logistics.network.api.query

import com.google.gson.annotations.SerializedName

data class PasswordCheckQuery (
    @SerializedName("tmpPassword") var tmpPassword: String
)