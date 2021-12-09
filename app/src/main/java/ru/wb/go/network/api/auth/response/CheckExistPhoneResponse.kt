package ru.wb.go.network.api.auth.response

import com.google.gson.annotations.SerializedName

@Deprecated("")
data class CheckExistPhoneResponse (
    @SerializedName("has_password") var hasPassword: Boolean,
    @SerializedName("use_sms") var useSms: Boolean
)