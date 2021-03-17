package com.wb.logistics.network.api.response

import com.google.gson.annotations.SerializedName

data class CheckExistPhoneResponse (
    @SerializedName("has_password") var hasPassword: Boolean,
    @SerializedName("use_sms") var useSms: Boolean
)