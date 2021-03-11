package com.wb.logistics.ui.config.dao

import com.google.gson.annotations.SerializedName

data class ConfigDAO(
    @SerializedName("auth_servers") val authServers: List<KeyValueDAO>,
    @SerializedName("api_servers") val apiServers: List<KeyValueDAO>
)