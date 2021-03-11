package com.wb.logistics.network.api.remote

import com.google.gson.annotations.SerializedName

data class CheckPhoneRemote (
    @SerializedName("has_password") var hasPassword: Boolean,
    @SerializedName("use_sms") var useSms: Boolean
)