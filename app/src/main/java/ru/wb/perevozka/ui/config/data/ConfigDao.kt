package ru.wb.perevozka.ui.config.data

import com.google.gson.annotations.SerializedName

data class ConfigDao(
    @SerializedName("auth_servers") val authServers: List<KeyValueDao>,
    @SerializedName("app_servers") val appServers: List<KeyValueDao>
)