package com.wb.logistics.ui.config.data

import com.google.gson.annotations.SerializedName

data class KeyValueDao(
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String
)