package com.wb.logistics.ui.config.dao

import com.google.gson.annotations.SerializedName

class KeyValueDAO(
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String
)