package com.wb.logistics.ui.config.dao

import com.google.gson.annotations.SerializedName

data class ConfigDao(
    @SerializedName("auth_servers") val authServers: List<KeyValueDao>,
    @SerializedName("api_servers") val apiServers: List<KeyValueDao>
)